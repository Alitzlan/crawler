import scrapy
import logging
import pymssql
import os
import re
import socket

from scrapy.http import Request

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
# ports to communicate with java client
JAVA_PORT_RX = 5004
JAVA_PORT_TX = 5005

def NextURL():
    global sock, JAVA_PORT_RX
    ack = "::::"

    while True:
        sock.sendto(ack.encode(), (socket.gethostname(), JAVA_PORT_TX))
        #print "waiting for data"
        nextURL, addr = sock.recvfrom(2048)
        nextURL = nextURL.decode()
        print ("Got data %s" % nextURL)
        yield nextURL

class redditSpider(scrapy.Spider):
    name = "reddit"
    allowed_domains = ["www.reddit.com"]
    url = NextURL()
    start_urls = [
        "https://www.reddit.com/r/singapore"
    ]
    logger = logging.getLogger("scrapy")

    def __init__(self, *args, **kwargs):
        global logger
        super(redditSpider, self).__init__(*args, **kwargs)
        #self.start_urls = [kwargs.get('start_url')]
        sock.bind((socket.gethostname(), JAVA_PORT_RX))
        logger = logging.getLogger("scrapy")
        logger.setLevel(logging.INFO)

    def parse(self, response):
        logger.info("parse: parsing %s" % response.url)

        if "comments" in response.url:
            yield Request(url=response.url, callback=self.parseThread, dont_filter=True)
        elif "user" in response.url:
            yield Request(url=response.url, callback=self.parseUser, dont_filter=True)
        else:
            yield Request(url=response.url, callback=self.parseSubReddit, dont_filter=True)

        nextUrl = self.url.next()
        logger.info("parse: nextURL %s" % nextUrl)
        yield Request(url=nextUrl, callback=self.parse, dont_filter=True)

    def parseSubReddit(self, response):
        global logger
        global sock, JAVA_PORT_TX

        logger.info("parseSubReddit: Parsing url - %s" % response.url)

        div_list = response.xpath('//div[@id="siteTable"]/div[contains(@class, "thing")]')
        title_list = response.xpath('//div[@class="entry unvoted"]/p[@class="title"]/a/text()').extract()
        url_list = response.xpath('//div[@class="entry unvoted"]/ul/li[@class="first"]/a/@href').extract()

        for index, div in enumerate(div_list):
            logger.debug("%d: Title: %s Author: %s url: %s" % (index, title_list[index], div.xpath('@data-author').extract()[0], url_list[index]))
            logger.info("Found url: %s" % url_list[index])
            #yield Request(url=url_list[index], callback=self.parseThread)
            sock.sendto(url_list[index].encode(), (socket.gethostname(), JAVA_PORT_TX))

        nextLink = response.xpath('//div[@class="nav-buttons"]/span[@class="nextprev"]/a/@href').extract()
        if len(nextLink) > 1:
            nextLink = nextLink[1]
        else:
            nextLink = nextLink[0]

        logger.info("Found next url: %s" % nextLink)
        #yield Request(url=("%s" % nextLink), callback=self.parseSubReddit)
        sock.sendto(nextLink.encode(), (socket.gethostname(), JAVA_PORT_TX))

        nextUrl = self.url.next()
        logger.info("parseSubReddit: nextURL %s" % nextUrl)
        yield Request(url=nextUrl, callback=self.parse, dont_filter=True)


    def parseThread(self, response):
        global logger
        global sock, JAVA_PORT_TX
        logger.info("Parsing thread url: %s" % response.url)

        subreddit = re.search('\/r\/([a-z]|[A-Z]|[0-9])+', response.url).group(0)
        subredditURL = "https://www.reddit.com" + subreddit

        #yield Request(url=("%s" % subredditURL), callback=self.parseSubReddit)
        sock.sendto(subredditURL.encode(), (socket.gethostname(), JAVA_PORT_TX))


        # sql connection load server & credentials
        credentialFile = open(("%s/../credentials.txt" % os.path.dirname(os.path.realpath(__file__))), 'r')
        server = credentialFile.readline().strip()
        user = credentialFile.readline().strip()
        password = credentialFile.readline().strip()
        credentialFile.close()

        conn = pymssql.connect(server,user,password,"cs505")
        cursor = conn.cursor()

        # extract the author of this thread
        authorResponse = response.xpath('//div[@id="siteTable"]/div/div[@class="entry unvoted"]/p/a[contains(@class, "author may-blank")]')
        author = authorResponse.xpath('text()').extract()[0]
        authorUrl = authorResponse.xpath('@href').extract()[0]

        #yield Request(url=("%s" % authorUrl), callback=self.parseUser)
        sock.sendto(authorUrl.encode(), (socket.gethostname(), JAVA_PORT_TX))

        # insert author into database
        logger.debug("INSERTING AUTHOR: %s" % author)
        cursor.callproc('insert_author', (str(author), 0))
        conn.commit()

        # count how many comments there are on the current thread
        listOfComments = response.xpath('//div[@class="entry unvoted"]').extract()
        rank = len(listOfComments) - 1              # subtract 1 due to the fact that the original post sames the same div id value

        # update users rank in database
        logger.debug("UPDATING AUTHOR: %s RANK: %d" % (author, rank))
        cursor.callproc('update_rank', (str(author), rank))
        conn.commit()

        # get the usernames of all people who commented on this thread
        listOfCommentersHTML = response.xpath('//a[contains(@class, "author may-blank")]')
        listOfCommenters = listOfCommentersHTML.xpath('text()').extract()
        listOfUsersURL = listOfCommentersHTML.xpath('@href').extract()

        for user in listOfCommenters:
            logger.debug("Author: %s\t Com: %s" % (author, user))
            if author != user:
                cursor.callproc('insert_relationship', (author, user, subreddit))
                conn.commit()

        conn.close()

        for userURL in listOfUsersURL:
            #yield Request(url=("%s" % userURL), callback=self.parseUser)
            sock.sendto(userURL.encode(), (socket.gethostname(), JAVA_PORT_TX))

        nextUrl = self.url.next()
        logger.info("parseThread: nextURL %s" % nextUrl)
        yield Request(url=nextUrl, callback=self.parse, dont_filter=True)

    def parseUser(self, response):
        global logger
        global sock, JAVA_PORT_TX
        logger.info("Parsing user url: %s" % response.url)
        commentedThreads = response.xpath('//a[contains(@class, "bylink may-blank")]').extract()
        for thread in commentedThreads:
            #yield Request(url=("%s" % thread), callback=self.parseThread)
            sock.sendto(thread.encode(), (socket.gethostname(), JAVA_PORT_TX))

        nextUrl = self.url.next()
        logger.info("parseUser: nextURL %s" % nextUrl)
        yield Request(url=nextUrl, callback=self.parse, dont_filter=True)
