FROM openjdk:12.0.1-jdk-oracle
ADD tsf-alauda.jar /
ADD run.sh /
RUN chmod 755 /run.sh
WORKDIR /
ENV PORT=""
ENV CONTEXT=""
ENV ENDPOINT=""
CMD ["/run.sh"]
