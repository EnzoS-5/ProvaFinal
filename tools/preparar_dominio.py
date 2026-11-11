#!/usr/bin/env python3
"""Renomeia o exemplo e seu campo principal antes de implementar o enunciado."""
import argparse
import json
from pathlib import Path
import re
import subprocess

root = Path(__file__).resolve().parents[1]
p = argparse.ArgumentParser(description=__doc__)
p.add_argument('--entidade', required=True, help='Classe singular, como Tarefa, Curso ou Produto')
p.add_argument('--rota', required=True, help='Nome plural sem barra, como tarefas')
p.add_argument('--campo-nome', default='nome', help='Campo principal, como titulo')
p.add_argument('--titulo', help='Título exibido no front, como Tarefas')
p.add_argument('--aplicar', action='store_true', help='Sem esta opção, mostra apenas o plano')
args = p.parse_args()
if not re.fullmatch(r'[A-Z][A-Za-z0-9]*', args.entidade):
    p.error('--entidade deve ser um identificador Java começando com letra maiúscula')
if not re.fullmatch(r'[a-z][a-z0-9-]*', args.rota):
    p.error('--rota deve conter letras minúsculas, números ou hífen')
if not re.fullmatch(r'[a-z][A-Za-z0-9]*', args.campo_nome):
    p.error('--campo-nome deve ser um identificador Java em camelCase')
if args.campo_nome in {'id', 'class', 'public', 'private', 'static', 'status', 'prioridade', 'descricao', 'ativo', 'dataCriacao'}:
    p.error('--campo-nome conflita com um campo existente ou palavra reservada')
schema_path = root / 'frontend/src/resource.json'
schema = json.loads(schema_path.read_text(encoding='utf-8'))
old_entity = schema.get('entity', 'Registro')
old_field = schema['titleField']
old_route = schema['endpoint']
new_route = '/' + args.rota
old_cap = old_field[0].upper() + old_field[1:]
new_cap = args.campo_nome[0].upper() + args.campo_nome[1:]
files = subprocess.check_output(['git', 'ls-files', '--cached', '--others', '--exclude-standard'], cwd=root, text=True).splitlines()
changes = []
for name in files:
    source = root / name
    if name.startswith('tools/') or source.suffix not in {'.java', '.xml', '.properties', '.jsx', '.js', '.json', '.md', '.yml', '.yaml'} and not name.endswith('.env.example') and name != '.env.example':
        continue
    if name.endswith('package-lock.json') or not source.is_file():
        continue
    text = source.read_text(encoding='utf-8')
    changed = text.replace(old_entity, args.entidade)
    changed = changed.replace(old_cap, new_cap)
    changed = re.sub(r'(?<![\w-])' + re.escape(old_field) + r'(?![\w-])', args.campo_nome, changed)
    changed = changed.replace(old_route, new_route)
    for quote in [chr(34), chr(39)]:
        changed = changed.replace(quote + old_route.strip('/'), quote + args.rota)
    # O nome da tabela acompanha a rota. Auditoria e o pacote .registro permanecem estáveis.
    if source.suffix == '.java':
        changed = changed.replace('"' + old_route.strip('/') + '"', '"' + args.rota + '"')
    target = source.with_name(source.name.replace(old_entity, args.entidade)) if source.suffix == '.java' else source
    if changed != text or target != source:
        changes.append((source, target, changed))
print(f'{old_entity} → {args.entidade}; {old_route} → {new_route}; {old_field} → {args.campo_nome}')
for source, target, _ in changes:
    print('  ' + str(source.relative_to(root)) + (' → ' + str(target.relative_to(root)) if target != source else ''))
if not args.aplicar:
    print('\nPrévia. Acrescente --aplicar para executar. Faça isso antes das alterações manuais da prova.')
    raise SystemExit(0)
if subprocess.check_output(['git', 'status', '--porcelain'], cwd=root, text=True).strip():
    p.error('A árvore de trabalho precisa estar limpa. Faça um commit da base antes de aplicar a adaptação.')
for source, target, _ in changes:
    if target != source and target.exists():
        p.error('Arquivo de destino já existe: ' + str(target.relative_to(root)))
for source, target, text in changes:
    target.write_text(text, encoding='utf-8')
    if target != source:
        source.unlink()
# Atualiza metadados de interface e guarda o nome para uma próxima adaptação.
schema = json.loads(schema_path.read_text(encoding='utf-8'))
schema.update(entity=args.entidade, endpoint=new_route, singular=args.entidade.lower(), plural=args.rota,
              title=args.titulo or args.rota.capitalize(), titleField=args.campo_nome)
schema['filter'] = {'field': args.campo_nome, 'label': 'Filtrar pelo início de ' + args.campo_nome}
for field in schema['fields']:
    if field['name'] == args.campo_nome:
        field.update(label=new_cap, placeholder=new_cap + ' do cadastro')
schema_path.write_text(json.dumps(schema, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print('\nAdaptação aplicada. Revise DTO, entidade, regras, schema do front e testes conforme o enunciado.')
print('Execute ./mvnw -U clean verify e, no front, npm test e npm run build.')
print('O script renomeia a base; não inventa os campos nem as regras da prova.')
