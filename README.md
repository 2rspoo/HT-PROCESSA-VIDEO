Grupo 82

Contribuições: Camila Rabello Spoo Goshima - Discord: camilaspoo - 11 973091025 

Rodrigo Rabello Spoo - Discord: srsinistro9459 - 11 981046096

⚙️ Processa Vídeo (Worker Assíncrono)

Este projeto é um microsserviço worker (trabalhador) que opera em background, sem interação direta com os usuários. Ele é acionado através de mensagens da fila AWS SQS. Sua única responsabilidade é realizar o download de um vídeo cru do AWS S3, extrair os frames em formato de imagem usando FFmpeg, compactar tudo em um arquivo .zip, fazer o upload do resultado de volta para o S3 e notificar a conclusão (ou falha) em uma fila de respostas.

Por atuar nos bastidores e não expor APIs REST complexas para o mundo externo, ele adere aos princípios de Arquitetura Hexagonal focando exclusivamente na orquestração de processamento.

🏛️ Arquitetura

A aplicação isola a pesada camada de processamento de vídeo das regras de domínio:

Domain: O modelo VideoMetadata que transita pelo fluxo de trabalho.

Application (Use Cases): O orquestrador central ProcessVideoCommand que define a ordem das operações.

Ports (In/Out): Contratos para leitura/escrita no S3, execução de comandos locais do sistema (VideoProcessorPort) e publicação de resultados na fila.

Infrastructure (Adapters): Adaptador SQS (Listener e Sender), S3 (Storage) e a implementação de conversão utilizando FFmpeg via ProcessBuilder.

🛠️ Tecnologias Utilizadas

Linguagem: Java 21

Framework: Spring Boot 3.4.1

Processamento de Mídia: FFmpeg & utilitários zip

Cloud/AWS: S3 (Armazenamento), SQS (Mensageria)

Qualidade: JaCoCo, SonarQube

Containerização: Docker (Imagem construída com binários do ffmpeg) & Kubernetes

🚀 Como Rodar o Projeto

Pré-requisitos

Java 21 SDK

Maven

Docker

FFmpeg instalado no sistema operacional (se rodar fora do Docker)

Credenciais da AWS configuradas

Configuração de Ambiente
Configure as variáveis em application.properties ou injete no cluster Kubernetes:

aws.region=us-east-1
aws.s3.bucket=seu-bucket-aqui
aws.sqs.queue-url=url-da-fila-de-envio-dos-videos
aws.sqs.results-queue-url=url-da-fila-de-respostas


Executando a Aplicação (Docker Recomendado)
Como este microsserviço depende do FFmpeg, a forma mais segura de rodá-lo localmente é via Docker, para garantir que o pacote multimídia esteja instalado no ambiente de execução:

docker build -t processa-video .
docker run --env-file .env processa-video


🔌 APIs e Observabilidade

Como é um Worker, este serviço não expõe endpoints de negócio, mas possui APIs internas de monitoramento.

Método

Endpoint

Descrição

GET

/actuator/health

Verifica a saúde da aplicação para o Kubernetes (Liveness/Readiness probes).

GET

/actuator/metrics

Métricas de performance do consumo de CPU pelo FFmpeg.

Fluxo de Execução Principal:
A aplicação é guiada por Eventos. Todo o ciclo de vida se inicia na classe SqsVideoListener.java, não possuindo interfaces visuais para acesso web direto.
