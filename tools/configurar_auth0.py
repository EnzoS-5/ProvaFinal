#!/usr/bin/env python3
"""Prepara configurações locais sem gravar credenciais no código público."""
import argparse
import json
from pathlib import Path
import re
import secrets

root = Path(__file__).resolve().parents[1]
p = argparse.ArgumentParser(description=__doc__)
p.add_argument('--domain', required=True, help='Tenant Auth0 ou domínio HTTPS customizado')
p.add_argument('--client-id', required=True)
p.add_argument('--audience', required=True, help='Identifier da API própria, não a Management API')
p.add_argument('--frontend-url', help='Origem HTTPS publicada, quando houver')
p.add_argument('--backend-url', help='URL do back AWS, como http://IP:8083')
args = p.parse_args()
domain = args.domain.removeprefix('https://').rstrip('/')
if not re.fullmatch(r'[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)+', domain):
    p.error('Informe só o hostname do Auth0, sem caminho, query ou protocolo HTTP')
for value in [args.client_id, args.audience, args.frontend_url or '', args.backend_url or '']:
    if any(char in value for char in ['\n', '\r', "'", '"', '$', '#']):
        p.error('Configuração contém caracteres não aceitos. Use os valores simples indicados no README.')
paths = [root / '.env', root / 'frontend/.env.local']
if any(path.exists() for path in paths):
    p.error('.env ou frontend/.env.local já existe. Edite o arquivo existente para preservar a senha do banco.')
endpoint = '/registros'
origins = 'http://localhost:5173' + (',' + args.frontend_url.rstrip('/') if args.frontend_url else '')
backend = '\n'.join([
    'DB_NAME=provafinal', 'DB_USER=prova', 'DB_PASSWORD=' + secrets.token_urlsafe(24),
    'AUTH0_ISSUER=https://' + domain + '/', 'AUTH0_AUDIENCE=' + args.audience,
    'FRONTEND_ORIGINS=' + origins, 'APP_PORT=8080', 'API_RESOURCE_PATH=' + endpoint,
    'API_READ_ROLES=ADMIN,USER', 'API_WRITE_ROLES=ADMIN', ''
])
frontend = '\n'.join([
    'VITE_AUTH0_DOMAIN=' + domain, 'VITE_AUTH0_CLIENT_ID=' + args.client_id,
    'VITE_AUTH0_AUDIENCE=' + args.audience, 'VITE_API_URL=/api', 'VITE_RESOURCE_PATH=' + endpoint,
    'VITE_READ_ROLES=ADMIN,USER', 'VITE_WRITE_ROLES=ADMIN',
    *(['BACKEND_URL=' + args.backend_url.rstrip('/')] if args.backend_url else []), ''
])
for path, value in zip(paths, [backend, frontend]):
    path.write_text(value, encoding='utf-8')
    try: path.chmod(0o600)
    except OSError: pass
print('Criados .env e frontend/.env.local. A senha local foi gerada e não foi exibida.')
print('Cadastre as origens de callback/logout/web no Auth0 e publique deploy/auth0-post-login.js.')
print('Para a Vercel, cadastre as mesmas variáveis VITE_* e BACKEND_URL no painel da hospedagem.')
