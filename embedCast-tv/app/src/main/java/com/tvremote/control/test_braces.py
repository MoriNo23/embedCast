import re
text = open("/home/fullmetal/Descargas/MovieON/tv-app/app/src/main/java/com/tvremote/control/MainActivity.kt").read()
# remove triple quoted strings
text = re.sub(r'"""[\s\S]*?"""', '""', text)
# remove single quoted strings
text = re.sub(r'"[\s\S]*?"', '""', text)
# remove single line comments
text = re.sub(r'//.*', '', text)
# remove multi line comments
text = re.sub(r'/\*[\s\S]*?\*/', '', text)

count = 0
for i, line in enumerate(text.splitlines(), 1):
    for c in line:
        if c == '{': count += 1
        elif c == '}': count -= 1
    if count < 0:
        print(f"Negative count at line {i} (mapped)")

print(f"Final true count: {count}")
