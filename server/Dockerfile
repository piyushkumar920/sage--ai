FROM node:22-alpine

WORKDIR /app

# Install production dependencies deterministically
COPY package*.json ./
RUN npm ci --only=production

# Copy server code
COPY server.js ./

# Cloud Run default port
EXPOSE 8080
ENV PORT=8080
ENV NODE_ENV=production

# Deterministic foreground startup command - single Node.js process, zero Nginx, zero frontend
CMD ["node", "server.js"]
