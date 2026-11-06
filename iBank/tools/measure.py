#!/usr/bin/env python3
"""iBank measurement tool (SOEN 6611 D2, Problems 5 and 6).

Counts Physical SLOC and Logical SLOC per Java file and computes the Posnett-Hindle-Devanbu
readability score per method. Only production code (src/main/java) is measured; test code
is excluded as the project description requires.

Usage: python3 tools/measure.py [source_root] [output_dir]
"""
import csv
import math
import os
import re
import sys
from collections import Counter

KEYWORDS = set("""abstract assert boolean break byte case catch char class const continue default
do double else enum extends final finally float for goto if implements import instanceof int
interface long native new package private protected public return short static strictfp super
switch synchronized this throw throws transient try void volatile while var true false null
""".split())
CONTROL = {"if", "else", "for", "while", "do", "switch", "try", "catch", "finally",
           "synchronized"}
TOKEN_RE = re.compile(r"""
    (?P<ws>\s+)
  | (?P<lcom>//[^\n]*)
  | (?P<bcom>/\*.*?\*/)
  | (?P<str>"(?:\\.|[^"\\])*")
  | (?P<chr>'(?:\\.|[^'\\])+')
  | (?P<num>\d[\d_]*(?:\.\d+)?[lLfFdD]?)
  | (?P<id>[A-Za-z_$][\w$]*)
  | (?P<op>->|::|\+\+|--|&&|\|\||[=!<>]=|[-+*/%&|^]=|<<=|>>=|\.\.\.|[{}()\[\];,.@=<>!~?:+\-*/&|^%])
""", re.S | re.X)


def tokenize(text):
    """Returns (kind, value, line) for code tokens, and the set of lines holding code."""
    tokens, code_lines, pos, line = [], set(), 0, 1
    while pos < len(text):
        m = TOKEN_RE.match(text, pos)
        if not m:
            raise ValueError(f"cannot tokenize at line {line}: {text[pos:pos+20]!r}")
        kind, value = m.lastgroup, m.group()
        if kind not in ("ws", "lcom", "bcom"):
            if kind == "id" and value in KEYWORDS:
                kind = "kw"
            tokens.append((kind, value, line))
            for k in range(value.count("\n") + 1):
                code_lines.add(line + k)
        line += value.count("\n")
        pos = m.end()
    return tokens, code_lines


def physical(text, code_lines):
    lines = text.split("\n")
    if lines and lines[-1] == "":
        lines.pop()
    blank = sum(1 for l in lines if not l.strip())
    return {"total": len(lines), "blank": blank, "physical": len(code_lines),
            "comment_only": len(lines) - blank - len(code_lines)}


def strip_annotations(stmt):
    out, i = [], 0
    while i < len(stmt):
        if stmt[i][1] == "@" and i + 1 < len(stmt) and stmt[i + 1][1] != "interface":
            i += 2
            if i < len(stmt) and stmt[i][1] == "(":
                depth = 0
                while i < len(stmt):
                    depth += stmt[i][1] == "("
                    depth -= stmt[i][1] == ")"
                    i += 1
                    if depth == 0:
                        break
            continue
        out.append(stmt[i])
        i += 1
    return out


def logical(tokens):
    """Logical SLOC under the counting scheme stated in the README / slides.

    R1 each ';' outside parentheses (statement, declaration, import, package) = 1
    R2 each control keyword if / else (not followed by if) / for / while / do / switch /
       try / catch / finally / case / default: = 1
    R3 each type declaration (class, interface, enum) and each method or constructor
       declaration with a body = 1
    R4 an enum constant list not terminated by ';' = 1
    Not counted: braces, annotations, blank and comment lines, lambda arrows.
    """
    counts = Counter()
    methods = []                    # (name, start_index, end_index)
    stmt, paren = [], 0
    brace_stack = []                # kinds of open blocks: 'type', 'enum', 'method', 'block'
    enum_has_semicolon = []
    for i, (kind, value, line) in enumerate(tokens):
        nxt = tokens[i + 1][1] if i + 1 < len(tokens) else ""
        if value == "(":
            paren += 1
        elif value == ")":
            paren -= 1
        if kind == "kw" and value in CONTROL | {"case"}:
            if not (value == "else" and nxt == "if"):
                counts["R2 control"] += 1
        if value == "default" and nxt == ":":
            counts["R2 control"] += 1
        if value == ";" and paren == 0:
            counts["R1 semicolon"] += 1
            if brace_stack and brace_stack[-1][0] == "enum":
                enum_has_semicolon[-1] = True
            stmt = []
            continue
        if value == "{":
            s = strip_annotations(stmt)
            vals = [t[1] for t in s]
            block = "block"
            is_type = any(v in ("class", "interface", "enum") and (j == 0 or vals[j - 1] != ".")
                          for j, v in enumerate(vals))
            if s and vals[0] in CONTROL:
                pass
            elif is_type:
                counts["R3 declaration"] += 1
                block = "enum" if "enum" in vals else "type"
            elif s and not ({"=", "->", "new", "return"} & set(vals)) and ")" in vals:
                counts["R3 declaration"] += 1
                block = "method"
                name = vals[vals.index("(") - 1]
                start = i - len(stmt)
                methods.append([name, start, None])
            brace_stack.append((block, len(methods) - 1 if block == "method" else None))
            if block == "enum":
                enum_has_semicolon.append(False)
            stmt = []
            continue
        if value == "}":
            block, midx = brace_stack.pop() if brace_stack else ("block", None)
            if block == "enum":
                if not enum_has_semicolon.pop():
                    counts["R4 enum list"] += 1
            if block == "method":
                methods[midx][2] = i
            stmt = []
            continue
        stmt.append((kind, value, line))
    return counts, methods


def readability(toks):
    """Posnett, Hindle and Devanbu (MSR 2011): z = 8.87 - 0.033 V + 0.40 L - 1.5 H."""
    operands = [t[1] for t in toks if t[0] in ("id", "num", "str", "chr")]
    operators = [t[1] for t in toks if t[0] not in ("id", "num", "str", "chr")]
    length = len(operands) + len(operators)
    vocabulary = len(set(operands)) + len(set(operators))
    volume = length * math.log2(vocabulary) if vocabulary > 1 else 0.0
    freq = Counter(t[1] for t in toks)
    entropy = -sum(c / length * math.log2(c / length) for c in freq.values())
    lines = toks[-1][2] - toks[0][2] + 1
    z = 8.87 - 0.033 * volume + 0.40 * lines - 1.5 * entropy
    return volume, lines, entropy, z, 1 / (1 + math.exp(-z))


def main():
    root = sys.argv[1] if len(sys.argv) > 1 else "src/main/java"
    out = sys.argv[2] if len(sys.argv) > 2 else "measurements"
    os.makedirs(out, exist_ok=True)
    files, methods_rows = [], []
    for dirpath, _, names in os.walk(root):
        for n in sorted(names):
            if n.endswith(".java"):
                files.append(os.path.join(dirpath, n))
    files.sort()
    file_rows = []
    for path in files:
        text = open(path, encoding="utf-8").read()
        tokens, code_lines = tokenize(text)
        p = physical(text, code_lines)
        counts, methods = logical(tokens)
        rel = os.path.relpath(path, root).replace(os.sep, "/")
        cls = rel.rsplit("/", 1)[-1][:-5]
        pkg = rel.rsplit("/", 2)[-2] if rel.count("/") >= 4 else "ibank"
        lsloc = sum(counts.values())
        file_rows.append({"package": pkg, "class": cls, **p, "logical": lsloc,
                          "R1": counts["R1 semicolon"], "R2": counts["R2 control"],
                          "R3": counts["R3 declaration"], "R4": counts["R4 enum list"],
                          "methods": len(methods)})
        for name, start, end in methods:
            v, l, h, z, r = readability(tokens[start:end + 1])
            methods_rows.append({"class": cls, "method": name, "line": tokens[start][2],
                                 "lines": l, "volume": round(v, 1), "entropy": round(h, 3),
                                 "z": round(z, 2), "readability": round(r, 4)})
    with open(os.path.join(out, "sloc_per_class.csv"), "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=list(file_rows[0]))
        w.writeheader()
        w.writerows(file_rows)
    with open(os.path.join(out, "readability_per_method.csv"), "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=list(methods_rows[0]))
        w.writeheader()
        w.writerows(methods_rows)
    tot = Counter()
    for r in file_rows:
        for k in ("total", "blank", "comment_only", "physical", "logical", "R1", "R2", "R3", "R4",
                  "methods"):
            tot[k] += r[k]
    print(f"Files: {len(file_rows)}")
    for k in ("total", "blank", "comment_only", "physical", "logical", "R1", "R2", "R3", "R4",
              "methods"):
        print(f"{k:>13}: {tot[k]}")
    rs = [m["readability"] for m in methods_rows]
    readable = sum(1 for r in rs if r >= 0.5)
    print(f"Methods: {len(rs)}  readable (>=0.5): {readable} ({100 * readable / len(rs):.1f}%)")
    print(f"Mean readability: {sum(rs) / len(rs):.3f}  median: {sorted(rs)[len(rs) // 2]:.3f}")


if __name__ == "__main__":
    main()
