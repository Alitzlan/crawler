import logging

from scrapy import cmdline

if __name__ == '__main__':
    # configure logging settings for crawler
    logger = logging.getLogger(__name__)

    logging.basicConfig(filename="crawler.log",                                                 # file name of log file
                        format='%(asctime)s %(module)s %(levelname)s - %(message)s',                       # format of log file
                        datefmt='%m/%d/%Y %I:%M:%S %p',                                         # datestamp format of log entry
                        level=logging.INFO)                                                     # set logging level to info

    cmdline.execute("scrapy crawl reddit".split())
    #cmdline.execute(("scrapy crawl reddit -a start_url=%s" % "https://www.reddit.com/r/BeardedDragons").split())