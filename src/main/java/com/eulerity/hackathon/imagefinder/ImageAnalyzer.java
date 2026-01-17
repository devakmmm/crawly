package com.eulerity.hackathon.imagefinder;

/**
 * ImageAnalyzer class to categorize and analyze images.
 * Can identify potential logos and categorize images.
 */
public class ImageAnalyzer {
    
    /**
     * Analyzes an image URL to determine if it might be a logo
     */
    public static boolean isPotentialLogo(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }
        
        String lowerUrl = imageUrl.toLowerCase();
        
        // Check for common logo-related keywords in URL
        String[] logoKeywords = {
            "logo", "brand", "favicon", "icon", "mark", 
            "emblem", "badge", "symbol", "identity"
        };
        
        for (String keyword : logoKeywords) {
            if (lowerUrl.contains(keyword)) {
                return true;
            }
        }
        
        // Check if it's a favicon
        if (lowerUrl.contains("favicon") || lowerUrl.endsWith("/favicon.ico")) {
            return true;
        }
        
        // Check if URL path suggests it's a logo (e.g., /logos/, /branding/)
        String[] logoPaths = {"/logo", "/logos", "/brand", "/branding", "/assets/logo"};
        for (String path : logoPaths) {
            if (lowerUrl.contains(path)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Categorizes an image based on its URL and characteristics
     */
    public static ImageCategory categorizeImage(String imageUrl) {
        if (isPotentialLogo(imageUrl)) {
            return ImageCategory.LOGO;
        }
        
        String lowerUrl = imageUrl.toLowerCase();
        
        // Check for common image categories
        if (lowerUrl.contains("avatar") || lowerUrl.contains("profile")) {
            return ImageCategory.AVATAR;
        }
        
        if (lowerUrl.contains("thumbnail") || lowerUrl.contains("thumb")) {
            return ImageCategory.THUMBNAIL;
        }
        
        if (lowerUrl.contains("banner") || lowerUrl.contains("hero")) {
            return ImageCategory.BANNER;
        }
        
        return ImageCategory.REGULAR;
    }
    
    /**
     * Image category enum
     */
    public enum ImageCategory {
        LOGO,
        AVATAR,
        THUMBNAIL,
        BANNER,
        REGULAR
    }
    
    /**
     * Image metadata class
     */
    public static class ImageMetadata {
        private final String url;
        private final ImageCategory category;
        private final boolean isLogo;
        
        public ImageMetadata(String url, ImageCategory category, boolean isLogo) {
            this.url = url;
            this.category = category;
            this.isLogo = isLogo;
        }
        
        public String getUrl() {
            return url;
        }
        
        public ImageCategory getCategory() {
            return category;
        }
        
        public boolean isLogo() {
            return isLogo;
        }
    }
}
