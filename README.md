# crawler
CS505 final project distributed crawler


## Python Environment setup for Xinu lab

### Install Scrapy
1. `cd ~`
2. `mkdir -p $HOME/python_local/lib/python2.7/site-packages`
3. setup PYTHONPATH environmental variable to include the new directory structure. (For fish shell it is: `set -x PYTHONPATH $HOME/python_local/lib/python2.7/site-packages`)
4. install python service_identity package:  `easy_install --prefix=$HOME/python_local service_identity`
5. install scrapy: `easy_install --prefix=$HOME/python_local scrapy`

### Install pymssql
1. `mkdir -p $HOME/local`
2. `wget ftp://ftp.freetds.org/pub/freetds/stable/freetds-patched.tar.gz`
3. `tar -xf freetds-patched.tar.gz`
4. `cd freetds-0.95.95`
5. `./configure --prefix=/homes/USERNAME/local`
6. `make`
7. `make install`
8. setup C_INCLUDE_PATH for easy_install (For fish shell it is: `set -x C_INCLUDE_PATH /homes/USERNAME/local/include`)
9. setup LIBRARY_PATH for easy_install (For fish shell it is:  `set -x LIBRARY_PATH /homes/USERNAME/local/lib`)
10. `easy_install --prefix=$HOME/python_local pymssql`
