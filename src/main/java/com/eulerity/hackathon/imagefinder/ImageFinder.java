package com.eulerity.hackathon.imagefinder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(
    name = "ImageFinder",
    urlPatterns = {"/main"}
)
public class ImageFinder extends HttpServlet{
	private static final long serialVersionUID = 1L;

	protected static final Gson GSON = new GsonBuilder().create();

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		
		String path = req.getServletPath();
		String url = req.getParameter("url");
		
		System.out.println("Got request of:" + path + " with query param:" + url);
		
		if (url == null || url.trim().isEmpty()) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().print(GSON.toJson(Collections.singletonMap("error", "URL parameter is required")));
			return;
		}
		
		// Ensure URL has a protocol
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			url = "https://" + url;
		}
		
		try {
			// Create and run the crawler
			WebCrawler crawler = new WebCrawler(url);
			Set<String> images = crawler.crawl(url);
			
			// Convert to sorted list for consistent output
			List<String> imageList = new ArrayList<>(images);
			Collections.sort(imageList);
			
			// Create response with image metadata
			List<Map<String, Object>> imageData = new ArrayList<>();
			for (String imageUrl : imageList) {
				Map<String, Object> imgInfo = new HashMap<>();
				imgInfo.put("url", imageUrl);
				imgInfo.put("isLogo", ImageAnalyzer.isPotentialLogo(imageUrl));
				imgInfo.put("category", ImageAnalyzer.categorizeImage(imageUrl).toString());
				imageData.add(imgInfo);
			}
			
			// Return both simple array (for backward compatibility) and detailed data
			Map<String, Object> response = new HashMap<>();
			response.put("images", imageList);
			response.put("imageData", imageData);
			response.put("count", imageList.size());
			
			resp.getWriter().print(GSON.toJson(response));
			
		} catch (MalformedURLException e) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			resp.getWriter().print(GSON.toJson(Collections.singletonMap("error", "Invalid URL: " + e.getMessage())));
		} catch (Exception e) {
			System.err.println("Error processing crawl request: " + e.getMessage());
			e.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resp.getWriter().print(GSON.toJson(Collections.singletonMap("error", "Error crawling URL: " + e.getMessage())));
		}
	}
}
