FROM python:3.11-slim

RUN apt-get update && \
    apt-get install -y wget gnupg curl unzip chromium-driver && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY crawler/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

CMD ["python", "crawler/app.py"]
