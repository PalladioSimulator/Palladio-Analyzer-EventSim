FROM usgs/rserve:latest

MAINTAINER Sandro Koch <layornos@gmail.com>

USER root

RUN apt-get update && apt-get install libxml2-dev libcairo2-dev

USER rserve

RUN R -e 'install.packages(c("'Rserve'", "'data.table'", "'ggplot2'", "'XML'", "'svglite'"))' -e 'run.Rserve(config.file = "'${CONFIG_PATH}'")'

