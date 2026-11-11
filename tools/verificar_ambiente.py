#!/usr/bin/env python3
"""Verificações rápidas antes da prova; nunca exibe senhas, tokens ou PEM."""
from pathlib import Path
import os
import re
import shutil
import subprocess

root = Path(__file__).resolve().parents[1]
def check(name, cmd):
    if not shutil.which(cmd[0]):
        print('[FALTA] ' + name + ': comando não encontrado'); return
    result = subprocess.run(cmd, cwd=root, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    print(('[OK] ' if result.returncode == 0 else '[ERRO] ') + name + ': ' + result.stdout.splitlines()[0] if result.stdout.splitlines() else '[ERRO] ' + name)
check('Java (usar JDK 21)', ['java', '-version'])
check('Git', ['git', '--version'])
check('Node (usar linha 22)', ['node', '--version'])
check('Docker', ['docker', '--version'])
check('Compose', ['docker', 'compose', 'version'])
check('Daemon Docker', ['docker', 'info', '--format', '{{.ServerVersion}}'])
for path in [root / '.mvn/maven.config', Path.home() / '.m2/settings.xml']:
    if path.exists():
        value = path.read_text(encoding='utf-8', errors='replace')
        if re.search(r'<offline>\s*true\s*</offline>|(^|\s)(-o|--offline)(\s|$)', value):
            print('[ERRO] Maven offline em ' + str(path) + '. Desative antes de baixar dependências.')
for name in ['.env', 'frontend/.env.local']:
    path = root / name
    if not path.exists():
        print('[FALTA] ' + name + ': use tools/configurar_auth0.py ou copie o exemplo e preencha.')
    else:
        content = path.read_text(encoding='utf-8')
        print(('[PENDENTE] ' if re.search(r'SEU_|troque-esta-senha', content) else '[OK] ') + name + ' verificado sem mostrar os valores.')
print('Na IDE: desative Work offline/Toggle Offline Mode do Maven e selecione JDK 21 para Project e Maven Runner.')
