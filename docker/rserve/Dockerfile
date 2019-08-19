# Dotcloud ubuntu image
FROM r-base:3.6.0
MAINTAINER layornos@gmail.com 

ENV DEBIAN_FRONTEND=noninteractive

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

RUN mkdir -p /Users/layornos/tmp

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
EXPOSE 6311

# set the command
RUN mkdir /home/ruser
ADD Rserv.conf /home/ruser/Rserv.conf
ADD Rserv.sh /home/ruser/Rserv.sh
WORKDIR /home/ruser

ENTRYPOINT ["./Rserv.sh"]
