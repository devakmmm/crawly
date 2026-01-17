# Crawly 🕷️

**Need to extract every image from a website without paying $99/month? Same. So I built this.**

[![Live Demo](https://img.shields.io/badge/demo-live-brightgreen)](https://crawly-production.up.railway.app)
[![GitHub](https://img.shields.io/badge/github-repo-blue)](https://github.com/devakmmm/crawly)

![Crawly Screenshot](https://i.imgur.com/placeholder.png)

---

## What It Does

Enter any URL → Crawly finds every image on that website.

- 🚀 **Multi-threaded crawling** — 10 concurrent threads for speed
- 🎯 **Logo detection** — Automatically identifies brand assets
- 🔒 **Friendly crawling** — Rate limiting so you don't get blocked
- 📊 **Export options** — JSON, CSV, or bulk download
- 🌐 **Domain filtering** — Stays within the same domain
- ⚡ **Real-time stats** — Images found, pages scanned, time elapsed

---

## The Problem

Needed to audit a competitor's website for visual assets.

Every existing tool was either:
- Too expensive ($99/month for image scraping?)
- Too slow (10+ minute wait times)
- Too complicated (enterprise dashboards for a simple task)

So I built my own.

---

## Technical Highlights

| Challenge                          | Solution                                    |
| ---------------------------------- | ------------------------------------------- |
| Infinite loops from circular links | URL normalization + visited tracking        |
| Getting IP banned                  | Built-in rate limiting (500ms delays)       |
| Memory leaks                       | Thread-safe concurrent collections          |
| Race conditions                    | ExecutorService with proper synchronization |
| Duplicate pages                    | URL deduplication before queuing            |

---

## Built With

- **Backend:** Java, Servlet API, JSoup, Jetty
- **Frontend:** Vanilla JS, CSS animations
- **Deployment:** Railway

---

## Run Locally

```bash
git clone https://github.com/devakmmm/crawly.git
cd crawly
mvn clean package -DskipTests
mvn jetty:run
```

Open `http://localhost:8080`

---

## What I Learned

1. **Concurrency is hard** — But understanding it makes you a better engineer
2. **Edge cases matter** — Real-world HTML is messy
3. **Simple > Complex** — No frameworks, no bloat, just code that works

---

## About Me

I'm a software engineer who enjoys building tools that solve real problems.

Currently open to **backend** and **full-stack** opportunities.

📫 **Let's connect:** [LinkedIn](https://linkedin.com/in/yourprofile) | [Email](mailto:your@email.com)

---

<p align="center">
  <i>Built in one caffeinated evening ☕</i>
</p>
