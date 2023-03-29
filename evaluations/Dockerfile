FROM python:3.11.2-slim-buster

# Install needed environment
RUN python -m pip install --upgrade pip
RUN python -m pip install requests
RUN python -m pip install pandas
RUN python -m pip install datasets
RUN python -m pip install huggingface_hub
RUN python -m pip install fsspec
RUN python -m pip install aiohttp
RUN python -m pip install sacrebleu
RUN python -m pip install python-dotenv
RUN python -m pip install pyinstaller
RUN python -m pip install evaluate
RUN python -m pip install openpyxl
RUN python -m pip install nltk
RUN python -m pip install rouge_score

# Set directory
WORKDIR /app

# Create folders to map
RUN mkdir outputs
RUN mkdir inputs

COPY evaluate.py /app
COPY .env /app

CMD ["python", "evaluate.py"]