import os
import re

packs_dir = 'digia-ui/src/main/java/com/digia/digiaui/framework/components/dui_icons/packs'
files = [
    ('FilledIcons.kt', 'Filled'),
    ('OutlinedIcons.kt', 'Outlined'),
    ('RoundedIcons.kt', 'Rounded'),
    ('SharpIcons.kt', 'Sharp')
]

icon_map = {}

for file, variant in files:
    path = os.path.join(packs_dir, file)
    if os.path.exists(path):
        with open(path, 'r') as f:
            content = f.read()
            matches = re.findall(r'"([^"]+)" to \{ (Icons\.[^,]+),', content)
            for key, icon in matches:
                icon_map[key] = icon

print(f'Total icons: {len(icon_map)}')

# Generate enum
enum_lines = ['enum class DUIIcon {']
for key in sorted(icon_map.keys()):
    enum_name = key.upper().replace('-', '_')
    enum_lines.append(f'    {enum_name},')
enum_lines.append('}')

# Generate resolve
resolve_lines = ['fun DUIIcon.resolve(): ImageVector = when (this) {']
for key in sorted(icon_map.keys()):
    enum_name = key.upper().replace('-', '_')
    resolve_lines.append(f'    {enum_name} -> {icon_map[key]}')
resolve_lines.append('}')

# Write to file
with open('generated_icons.kt', 'w') as f:
    f.write('\n'.join(enum_lines) + '\n\n' + '\n'.join(resolve_lines) + '\n')

print('Generated generated_icons.kt')