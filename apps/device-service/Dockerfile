FROM python:3.12-alpine

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY models.py repository.py service.py app.py ./

EXPOSE 8082

CMD ["python", "app.py"]
