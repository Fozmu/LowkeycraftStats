# LowkeycraftStats Plugin - Build Instructions

## Requirements
- Java 17 or higher
- Maven 3.8 or higher

## Building the Plugin

### Option 1: Using Command Line (if you have Maven installed)
1. Open command prompt/terminal
2. Navigate to the plugin directory:
   ```
   cd C:\Users\danie\Desktop\lowkeycraft\LowkeycraftStats
   ```
3. Run Maven build:
   ```
   mvn clean package
   ```
4. The compiled JAR will be in `target/lowkeycraft-stats-1.0.0.jar`

### Option 2: Using IntelliJ IDEA
1. Open IntelliJ IDEA
2. Import the project (select the `pom.xml` file)
3. Wait for Maven to download dependencies
4. Go to Maven tab on right side
5. Expand `lowkeycraft-stats` > `Lifecycle`
6. Double-click `package`
7. JAR file will be in `target/` folder

### Option 3: Using VS Code
1. Install "Extension Pack for Java"
2. Open the project folder
3. Press `Ctrl+Shift+P` and type "Java: Build"
4. Select "Build Workspace"

## Installation on Server

1. Copy the generated JAR file to your server's `plugins/` folder
2. Restart the server or use a plugin manager to load it
3. Configure the plugin in `plugins/LowkeycraftStats/config.yml`

## Configuration

### Basic Setup
```yaml
database:
  type: sqlite  # Use SQLite for simplicity

web-server:
  enabled: true
  port: 8080    # Make sure this port is open on your server

tracking:
  inventory: true
  location: true
  health-food: true
  update-interval: 30  # seconds
```

### For Exaroton Servers
Make sure to:
1. Open port 8080 in Exaroton server settings
2. Set the web server port to an available port
3. Use SQLite database (easier than MySQL setup)

## API Endpoints

Once running, your plugin will provide these endpoints:

- `GET /api/health` - Check if API is running
- `GET /api/server/stats` - Server statistics
- `GET /api/player/{username}` - Player statistics
- `GET /api/players/online` - List of online players

## Updating Your Website

Replace the Exaroton API calls in your website with:

```javascript
// Instead of /api/player-stats from Netlify functions
const response = await fetch('http://your-server-ip:8080/api/player/Fozmu');
const playerData = await response.json();

// Instead of /api/server-status
const serverResponse = await fetch('http://your-server-ip:8080/api/server/stats');
const serverData = await serverResponse.json();
```

## Troubleshooting

### Common Issues:

1. **Port already in use**: Change the port in config.yml
2. **Database errors**: Make sure the plugin folder has write permissions
3. **API not accessible**: Check firewall/port settings
4. **Plugin not loading**: Check server logs for Java version compatibility

### Debug Mode
Enable debug in config.yml to see detailed logging:
```yaml
debug: true
```

## Features Included

✅ **Real-time player tracking**
✅ **Complete statistics** (playtime, blocks, deaths, kills, etc.)
✅ **Live player data** (health, food, XP, location)
✅ **Web API** for your website integration
✅ **SQLite database** (no external database needed)
✅ **In-game commands** (`/stats`, `/statsreload`)
✅ **Configurable tracking** options

## What This Replaces

**Before (Exaroton API limitations):**
- ❌ Only current online players
- ❌ No historical data
- ❌ No detailed statistics
- ❌ No inventory/health data

**After (LowkeycraftStats plugin):**
- ✅ All players who ever joined
- ✅ Complete historical data
- ✅ Detailed statistics tracking
- ✅ Live inventory, health, XP, location
- ✅ Real-time updates
- ✅ Custom API for your website

This plugin gives you 100% control over player data and eliminates all the limitations of the Exaroton API!