package com.eulerity.hackathon.imagefinder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebCrawler class that crawls a website to find all images.
 * Features:
 * - Multi-threaded crawling
 * - Domain restriction
 * - Visited URL tracking
 * - Rate limiting for friendly crawling
 */
public class WebCrawler {
    private static final int MAX_THREADS = 10;
    private static final int MAX_DEPTH = 3;
    private static final int REQUEST_DELAY_MS = 500; // 500ms delay between requests
    
    private final String baseDomain;
    private final Set<String> visitedUrls;
    private final Set<String> foundImages;
    private final ExecutorService executorService;
    private final BlockingQueue<String> urlQueue;
    private final AtomicInteger activeThreads;
    
    public WebCrawler(String startUrl) throws MalformedURLException {
        URL url = new URL(startUrl);
        this.baseDomain = url.getProtocol() + "://" + url.getHost();
        this.visitedUrls = Collections.synchronizedSet(new HashSet<>());
        this.foundImages = Collections.synchronizedSet(new HashSet<>());
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
        this.urlQueue = new LinkedBlockingQueue<>();
        this.activeThreads = new AtomicInteger(0);
    }
    
    /**
     * Starts the crawling process from the given URL
     */
    public Set<String> crawl(String startUrl) {
        urlQueue.offer(startUrl);
        visitedUrls.add(normalizeUrl(startUrl));
        
        // Start initial crawl
        submitCrawlTask(startUrl, 0);
        
        // Process queue until all URLs are crawled
        while (!urlQueue.isEmpty() || activeThreads.get() > 0) {
            try {
                String url = urlQueue.poll(1, TimeUnit.SECONDS);
                if (url != null) {
                    String normalizedUrl = normalizeUrl(url);
                    if (!visitedUrls.contains(normalizedUrl)) {
                        visitedUrls.add(normalizedUrl);
                        // Extract depth from URL if stored, otherwise use 0
                        int depth = extractDepth(url);
                        submitCrawlTask(url, depth);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        executorService.shutdown();
        try {
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return new HashSet<>(foundImages);
    }
    
    /**
     * Submits a crawl task to the thread pool
     */
    private void submitCrawlTask(String url, int depth) {
        if (depth >= MAX_DEPTH) {
            return;
        }
        
        activeThreads.incrementAndGet();
        executorService.submit(() -> {
            try {
                crawlPage(url, depth);
            } finally {
                activeThreads.decrementAndGet();
            }
        });
    }
    
    /**
     * Crawls a single page and extracts images and links
     */
    private void crawlPage(String urlString, int depth) {
        try {
            // Rate limiting - be friendly to the server
            Thread.sleep(REQUEST_DELAY_MS);
            
            Document doc = Jsoup.connect(urlString)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .followRedirects(true)
                    .get();
            
            // Extract images
            Elements imgElements = doc.select("img[src], img[data-src]");
            for (Element img : imgElements) {
                String imgSrc = img.attr("src");
                if (imgSrc.isEmpty()) {
                    imgSrc = img.attr("data-src");
                }
                if (!imgSrc.isEmpty()) {
                    String absoluteImgUrl = resolveUrl(urlString, imgSrc);
                    if (isValidImageUrl(absoluteImgUrl)) {
                        foundImages.add(absoluteImgUrl);
                    }
                }
            }
            
            // Also check for CSS background images and other sources
            Elements elementsWithBg = doc.select("[style*='background-image']");
            for (Element elem : elementsWithBg) {
                String style = elem.attr("style");
                String bgUrl = extractBackgroundImageUrl(style);
                if (bgUrl != null) {
                    String absoluteImgUrl = resolveUrl(urlString, bgUrl);
                    if (isValidImageUrl(absoluteImgUrl)) {
                        foundImages.add(absoluteImgUrl);
                    }
                }
            }
            
            // Extract links for further crawling (if not at max depth)
            if (depth < MAX_DEPTH - 1) {
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String href = link.attr("abs:href");
                    String normalizedHref = normalizeUrl(href);
                    if (isSameDomain(href) && !visitedUrls.contains(normalizedHref)) {
                        // Mark as visited before adding to queue to prevent duplicates
                        visitedUrls.add(normalizedHref);
                        urlQueue.offer(href + "|depth:" + (depth + 1));
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error crawling " + urlString + ": " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Resolves a relative URL to an absolute URL
     */
    private String resolveUrl(String baseUrl, String relativeUrl) {
        try {
            URL base = new URL(baseUrl);
            URL resolved = new URL(base, relativeUrl);
            return resolved.toString();
        } catch (MalformedURLException e) {
            return relativeUrl;
        }
    }
    
    /**
     * Checks if a URL belongs to the same domain
     */
    private boolean isSameDomain(String url) {
        try {
            URL urlObj = new URL(url);
            String domain = urlObj.getProtocol() + "://" + urlObj.getHost();
            return domain.equals(baseDomain);
        } catch (MalformedURLException e) {
            return false;
        }
    }
    
    /**
     * Normalizes a URL for comparison (removes fragments, trailing slashes, depth suffix)
     */
    private String normalizeUrl(String url) {
        // Remove depth suffix if present
        String cleanUrl = url;
        if (url.contains("|depth:")) {
            cleanUrl = url.substring(0, url.indexOf("|depth:"));
        }
        
        try {
            URL urlObj = new URL(cleanUrl);
            String normalized = urlObj.getProtocol() + "://" + urlObj.getHost() + urlObj.getPath();
            if (urlObj.getQuery() != null && !urlObj.getQuery().isEmpty()) {
                normalized += "?" + urlObj.getQuery();
            }
            return normalized.toLowerCase();
        } catch (MalformedURLException e) {
            return cleanUrl.toLowerCase();
        }
    }
    
    /**
     * Extracts depth from URL if stored in format "url|depth:N"
     */
    private int extractDepth(String url) {
        if (url.contains("|depth:")) {
            try {
                String depthStr = url.substring(url.indexOf("|depth:") + 7);
                return Integer.parseInt(depthStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    /**
     * Checks if a URL is a valid image URL
     */
    private boolean isValidImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        url = url.toLowerCase();
        return url.matches(".*\\.(jpg|jpeg|png|gif|bmp|webp|svg)(\\?.*)?$") 
            || url.contains("image") 
            || url.contains("img");
    }
    
    /**
     * Extracts background image URL from CSS style attribute
     */
    private String extractBackgroundImageUrl(String style) {
        if (style == null || style.isEmpty()) {
            return null;
        }
        int urlIndex = style.indexOf("url(");
        if (urlIndex == -1) {
            return null;
        }
        int start = urlIndex + 4;
        int end = style.indexOf(")", start);
        if (end == -1) {
            return null;
        }
        String url = style.substring(start, end).trim();
        // Remove quotes if present
        url = url.replaceAll("^['\"]|['\"]$", "");
        return url;
    }
}
