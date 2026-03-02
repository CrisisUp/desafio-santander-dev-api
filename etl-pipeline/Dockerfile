FROM python:3.11-slim
WORKDIR /app

# Instala dependências
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copia o código e o .env
COPY . .

# Comando para manter o container vivo ou rodar o script
RUN chmod +x entrypoint.sh
CMD ["./entrypoint.sh"]