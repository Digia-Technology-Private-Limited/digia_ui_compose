import re
with open('/Users/ram/Digia/digia_ui_compose/digia-ui/src/main/java/com/digia/digiaui/framework/components/dui_icons/packs/MaterialIcons.kt', 'r') as f:
    for line in f:
        match = re.match(r'^\s*iconMap\[\"([^\"]+)\"\] = ([^;]+);', line)
        if match:
            key, value = match.groups()
            print(f'"{key}" -> {value}')
