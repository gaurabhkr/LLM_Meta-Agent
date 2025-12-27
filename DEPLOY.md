# 🚀 Deployment Guide

This guide covers how to push your code to **GitHub** and deploy it using **Render**.

## 1. Push Code to GitHub

First, you need to get your code onto GitHub.

1.  **Initialize Git** (if you haven't already):
    ```bash
    git init
    ```

2.  **Add Files**:
    ```bash
    git add .
    ```

3.  **Commit Changes**:
    ```bash
    git commit -m "Initial commit - LLM Meta-Agent v1.0"
    ```

4.  **Create a Repository on GitHub**:
    *   Go to [GitHub.com](https://github.com) and log in.
    *   Click the **+** icon in the top right -> **New repository**.
    *   Name it `LLM-Meta-Agent` (or your preferred name).
    *   Click **Create repository**.

5.  **Connect and Push**:
    *   Copy the commands under **"…or push an existing repository from the command line"**. They will look like this:
    ```bash
    git remote add origin https://github.com/YOUR_USERNAME/LLM-Meta-Agent.git
    git branch -M main
    git push -u origin main
    ```
    *   Run these commands in your terminal.

## 2. Deploy on Render (Free & Easy)

Render is great for Spring Boot Docker apps.

1.  **Sign Up/Login** to [Render.com](https://render.com).
2.  Click **New +** and select **Web Service**.
3.  **Connect GitHub**: Select "Build and deploy from a Git repository" and connect your GitHub account.
4.  **Select Repository**: Find and select your `LLM-Meta-Agent` repository.
5.  **Configure Service**:
    *   **Name**: `llm-meta-agent`
    *   **Region**: Pick the closest one to you.
    *   **Runtime**: Select **Docker**. (Render will automatically find the `Dockerfile` in your project).
    *   **Instance Type**: Free (if available) or Starter.

6.  **Environment Variables** (Crucial!):
    *   Scroll down to the **Environment Variables** section.
    *   Click **Add Environment Variable**.
    *   **Key**: `OPENROUTER_API_KEY`
    *   **Value**: Paste your actual OpenRouter API key starting with `sk-or-...`.

7.  **Deploy**:
    *   Click **Create Web Service**.
    *   Render will now build your Docker image and deploy it. This might take a few minutes.

8.  **Done!**
    *   Once complete, you'll get a URL like `https://llm-meta-agent.onrender.com`. Click it to use your live AI agent!
