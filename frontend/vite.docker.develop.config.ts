import { networkInterfaces } from 'node:os'

import { defineConfig, mergeConfig } from 'vite'
import baseConfig  from './vite.config'


function logAvailableInterfaces(): void {
  const interfaces = networkInterfaces()
  const allIPs: string[] = []
  const tailscaleIPs: string[] = []

  for (const [name, addresses] of Object.entries(interfaces)) {
    const isTailscale = name.toLowerCase().includes('tailscale') ||
      name.toLowerCase().includes('ts') ||
      name === 'tailscale0'

    // Look for Docker bridge or local network interfaces
    for (const addr of addresses || []) {
      if (addr.family === 'IPv4' && !addr.internal) {
        const ip = addr.address

        if (ip.startsWith('100.') || isTailscale) {
          tailscaleIPs.push(`${ip} (${name} - Tailscale, excluded)`)
        } else {
          allIPs.push(`${ip} (${name})`)
        }
      }
    }
  }

  // Print available IPs
  console.log('\nAvailable network interfaces:')
  console.log(`  ➜  Selected: localhost - Docker can access via host.docker.internal`)
  allIPs.forEach(ip => console.log(`     Available: ${ip}`))
  if (tailscaleIPs.length > 0) {
    tailscaleIPs.forEach(ip => console.log(`     ${ip}`))
  }
  console.log('')
}

// Log interfaces on startup
logAvailableInterfaces()

// https://vite.dev/config/
export default mergeConfig(
  baseConfig,
  defineConfig({
  server: {
    host: '0.0.0.0', // Exclude Tailscale, allow Docker access via host.docker.internal
    port: 5173,
    watch: {
      usePolling: true,
      interval: 100
    },
    hmr: {
      clientPort: 3000
    },
    // Proxy API requests to Spring Boot backend
    proxy: {
      '/api': {
        target: 'http://backend:8080',
        changeOrigin: true,
        secure: false,
      },
      '/actuator': {
        target: 'http://backend:8080',
        changeOrigin: true,
        secure: false,
      }
    }
  },
})
)
