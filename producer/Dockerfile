FROM python:3.12.7-slim-bullseye

ENV PIP_DISABLE_PIP_VERSION_CHECK 1
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1
WORKDIR /usr/app/
COPY ./producer/requirements.txt /tmp/pip-tmp/requirements.txt

RUN pip3 --disable-pip-version-check --no-cache-dir install -r /tmp/pip-tmp/requirements.txt \
   && rm -rf /tmp/pip-tmp

COPY ./trade.avsc ./trade.avsc
COPY ./producer .

CMD ["python", "producer.py"]
