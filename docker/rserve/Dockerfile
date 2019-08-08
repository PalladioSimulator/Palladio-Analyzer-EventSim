# Dotcloud ubuntu image
FROM r-base
MAINTAINER layornos@gmail.com 

ENV DEBIAN_FRONTEND=noninteractive
#RUN apt-get update && apt-get install -y software-properties-common gnupg2 apt-utils libcairo2-dev
#RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys E298A3A825C0D65DFD57CBB651716619E084DAB9
#RUN add-apt-repository 'deb https://cloud.r-project.org/bin/linux/ubuntu bionic-cran35/'

# Update and install
RUN apt-get update && apt-get install -y \
  libcurl4-openssl-dev \
  libssl-dev \
  libxml2-dev \
  libmime-base64-urlsafe-perl \
  libdigest-hmac-perl \
  libdigest-sha-perl \
  libssl-dev \
  libapparmor1 \
  libpq-dev \
  gnupg2 \
  libcairo2-dev \
  libgdal-dev \
  wget

# log R version
RUN R --version

#install R packages
RUN echo 'install.packages(c("ggplot2"), repos="http://cran.us.r-project.org", dependencies=TRUE)' > /tmp/packages.R \
    && Rscript /tmp/packages.R

RUN echo 'install.packages("Rserve",,"http://rforge.net/",type="source")' > /tmp/packages2.R \
    && Rscript /tmp/packages2.R

# Popular data science packages
RUN echo 'install.packages(c("data.table", "ggplot2", "XML", "svglite"), repos="http://cran.us.r-project.org", dependencies=TRUE)' > /tmp/packages.R \
    && Rscript /tmp/packages.R
#RUN R -e "install.packages('packrat', repos='http://cran.r-project.org')"
#RUN R -e "install.packages(c('Rserve', 'data.table', 'ggplot2', 'XML', 'svglite'))"

# Add rserve user
#RUN groupadd -r rserve && useradd -r -g rserve rserve

# adding start R script
#ADD start.R start.R
#ADD Rserv.conf /Rserv.conf

# Create app directory
#RUN mkdir -p /usr/src/app
#WORKDIR /usr/src/app

# Bundle app source
#ONBUILD COPY *.R *.r /usr/src/app/
#ONBUILD RUN chown -R rserve:rserve /usr/src/app
#ONBUILD RUN su rserve -c "R -e \"packrat::init();\""
# ONBUILD RUN su rserve -c "R -e \"install.packages('Rserve', 'Rserve_1.8-6.tgz', 'http://www.rforge.net/')\""
#ONBUILD RUN su rserve -c "R -e \"install.packages('Rserve', repos='http://cran.r-project.org')\""
# ONBUILD RUN R -e "install.packages(c('Rserve', 'data.table', 'ggplot2', 'XML', 'svglite'))"


#RUN R -e "library(Rserve)"
#RUN R -e "Rserve(debug = TRUE, port = 6311, args = NULL, config.file = \"Rserv.conf\")"

#CMD [ "Rscript", "/start.R" ]
EXPOSE 6311

#CMD /bin/bash

# set the command
RUN mkdir /home/ruser
ADD Rserv.conf /home/ruser/Rserv.conf
ADD Rserv.sh /home/ruser/Rserv.sh
WORKDIR /home/ruser

ENTRYPOINT ["./Rserv.sh"]
