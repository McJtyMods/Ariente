#!/usr/bin/env python3
"""Audit Ariente assets, isolated datagen drift and (optionally) the release jar.

Run from any directory with Python 3; no third-party packages are needed.
"""
import argparse
import json
from pathlib import Path
import sys
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--generated', type=Path, help='isolated runClientData output')
    parser.add_argument('--jar', type=Path, help='NeoForge release jar')
    args = parser.parse_args()
    files = {}
    errors = []
    for root in (ROOT / 'src/main/resources', ROOT / 'src/generated/resources'):
        for path in root.rglob('*'):
            if path.is_file() and '.cache' not in path.parts:
                name = path.relative_to(root).as_posix()
                if name in files:
                    errors.append(f'Duplicate resource: {name}')
                files[name] = path
    docs = {}
    for name, path in files.items():
        if name.endswith(('.json', '.mcmeta')):
            try:
                docs[name] = json.loads(path.read_text())
            except ValueError as error:
                errors.append(f'{name}: {error}')

    def reference(identifier, kind, suffix):
        namespace, _, path = identifier.partition(':')
        if not path:
            namespace, path = 'minecraft', namespace
        if namespace != 'ariente' or path.startswith('builtin/'):
            return
        target = f'assets/{namespace}/{kind}/{path}{suffix}'
        if target not in files:
            errors.append(f'Missing {kind}: {identifier}')

    def models(value):
        if isinstance(value, dict):
            for key, child in value.items():
                if key == 'model' and isinstance(child, str):
                    reference(child, 'models', '.json')
                else:
                    models(child)
        elif isinstance(value, list):
            for child in value:
                models(child)

    for name, doc in docs.items():
        if '/models/' in name:
            if doc.get('loader') == 'forge:obj':
                namespace, path = doc['model'].split(':', 1)
                if namespace == 'ariente' and f'assets/{namespace}/{path}' not in files:
                    errors.append(f'Missing OBJ: {doc["model"]}')
            if 'parent' in doc:
                reference(doc['parent'], 'models', '.json')
            for texture in doc.get('textures', {}).values():
                if not texture.startswith('#'):
                    reference(texture, 'textures', '.png')
        if '/blockstates/' in name:
            models(doc)
        if name.endswith('/sounds.json'):
            for event in doc.values():
                for sound in event.get('sounds', []):
                    if isinstance(sound, str):
                        reference(sound, 'sounds', '.ogg')
                    elif sound.get('type', 'file') == 'file':
                        reference(sound['name'], 'sounds', '.ogg')
    if docs['pack.mcmeta']['pack']['min_format'] != [97, 1]:
        errors.append('Expected Minecraft 26.3 resource pack format 97.1')
    if args.generated:
        generated = list(args.generated.rglob('*.json'))
        if not generated:
            errors.append(f'No generated JSON in {args.generated}')
        for path in generated:
            if '.cache' in path.parts:
                continue
            name = path.relative_to(args.generated).as_posix()
            if name not in docs or docs[name] != json.loads(path.read_text()):
                errors.append(f'Datagen drift: {name}')
        for path in (ROOT / 'src/generated/resources').rglob('*.json'):
            name = path.relative_to(ROOT / 'src/generated/resources')
            if not (args.generated / name).is_file():
                errors.append(f'Stale generated file: {name}')
        print(f'Compared {len(generated)} generated JSON files')
    if args.jar:
        with ZipFile(args.jar) as jar:
            names = jar.namelist()
            if any(name.startswith('.cache/') for name in names):
                errors.append('Datagen cache in release jar')
            if len(names) != len(set(names)):
                errors.append('Duplicate jar entries')
            for name, path in files.items():
                if name not in names or jar.read(name) != path.read_bytes():
                    errors.append(f'Jar resource missing or stale: {name}')
            if any('/gameTest/' in n or n.endswith('Tests.class') for n in names):
                errors.append('Test classes in release jar')
            for name in ('mcjty/ariente/Ariente.class', 'META-INF/neoforge.mods.toml', 'META-INF/accesstransformer.cfg'):
                if name not in names:
                    errors.append(f'Missing jar entry: {name}')
            equipment_code = jar.read('mcjty/ariente/ForgeEventHandlers.class')
            if int.from_bytes(equipment_code[6:8], 'big') != 69:
                errors.append('Release jar must target Java 25')
            version = next(line.split('=', 1)[1] for line in (ROOT / 'gradle.properties').read_text().splitlines()
                           if line.startswith('version='))
            if f'Implementation-Version: {version}\r\n' not in jar.read('META-INF/MANIFEST.MF').decode():
                errors.append('Jar manifest version differs from gradle.properties')
            if not (ROOT / 'changelog.txt').read_text().startswith(version + ':\n'):
                errors.append('Changelog heading differs from gradle.properties')
    for error in sorted(set(errors)):
        print(error, file=sys.stderr)
    print(f'Audited {len(files)} resources ({len(docs)} JSON/metadata files); {len(set(errors))} errors')
    return bool(errors)


if __name__ == '__main__':
    sys.exit(main())
