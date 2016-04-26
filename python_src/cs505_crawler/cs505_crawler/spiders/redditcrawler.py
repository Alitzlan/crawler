import scrapy
import logging
import pymssql
import os

from scrapy.http import Request

class redditSpider(scrapy.Spider):
    name = "reddit"
    allowed_domains = ["www.reddit.com"]
    start_urls = [
        "http://www.reddit.com/r/programming"
    ]
    logger = logging.getLogger(__name__)

    def parse(self, response):
        if "comments" in response.url:
            yield Request(url=response.url, callback=self.parseThread)
        else:
            yield Request(url=response.url, callback=self.parseSubReddit)

    def parseSubReddit(self, response):
        logging.info("Parsing url: %s" % response.url)
        div_list = response.xpath('//div[@id="siteTable"]/div[contains(@class, "thing")]')
        title_list = response.xpath('//div[@class="entry unvoted"]/p[@class="title"]/a/text()').extract()
        url_list = response.xpath('//div[@class="entry unvoted"]/ul/li[@class="first"]/a/@href').extract()

        for index, div in enumerate(div_list):
            print ("%d: Title: %s Author: %s url: %s" % (index, title_list[index], div.xpath('@data-author').extract()[0], url_list[index]))
            yield Request(url=url_list[index], callback=self.parseThread)

        nextLink = response.xpath('//div[@class="nav-buttons"]/span[@class="nextprev"]/a/@href').extract()
        if len(nextLink) > 1:
            nextLink = nextLink[1]
        else:
            nextLink = nextLink[0]

        print nextLink
        yield Request(url=("%s" % nextLink), callback=self.parseSubReddit)




    def parseThread(self, response):
        logging.info("Parsing thread url: %s" % response.url)

        # sql connection load server & credentials
        credentialFile = open(("%s/../credentials.txt" % os.path.dirname(os.path.realpath(__file__))), 'r')
        server = credentialFile.readline().strip()
        user = credentialFile.readline().strip()
        password = credentialFile.readline().strip()
        credentialFile.close()

        conn = pymssql.connect(server,user,password,"cs505")
        cursor = conn.cursor()

        # extract the author of this thread
        author = response.xpath('//div[@id="siteTable"]/div/@data-author').extract()[0]

        # insert author into database
        cursor.callproc('insert_author', (str(author), 0))
        conn.commit()

        # count how many comments there are on the current thread
        listOfComments = response.xpath('//div[@class="entry unvoted"]').extract()
        rank = len(listOfComments) - 1              # subtract 1 due to the fact that the original post sames the same div id value

        # update users rank in database
        cursor.callproc('update_rank', (str(author), rank))
        conn.commit()

        # get the usernames of all people who commented on this thread
        listOfCommenters = response.xpath('//a[contains(@class, "author may-blank")]/text()').extract()
        for user in listOfCommenters:
            #print("Author: %s\t Com: %s" % (author, user))
            if author != user:
                cursor.callproc('insert_relationship', (author, user))
                conn.commit()

        conn.close()