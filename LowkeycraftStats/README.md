# LowkeycraftStats Plugin

A comprehensive Minecraft plugin for real-time player statistics tracking with web API integration.

## 🚀 Features

- **Real-time player tracking** - Health, food, XP, location
- **Complete statistics** - Playtime, blocks, deaths, kills, crafting
- **Web API** - REST endpoints for website integration
- **SQLite database** - No external database required
- **In-game commands** - `/stats` and `/statsreload`
- **Configurable tracking** - Enable/disable specific features

## 📥 Download

**Latest Release:** [Download JAR file from Releases](../../releases/latest)

## 🔧 Installation

1. Download the latest `lowkeycraft-stats-x.x.x.jar` from [Releases](../../releases)
2. Place it in your server's `plugins/` folder
3. Restart your server
4. Configure in `plugins/LowkeycraftStats/config.yml`

## ⚙️ Configuration

```yaml
database:
  type: sqlite
  file: stats.db

web-server:
  enabled: true
  port: 8080
  cors: true

tracking:
  inventory: true
  location: true
  health-food: true
  update-interval: 30
```

## 📊 API Endpoints

- `GET /api/health` - API health check
- `GET /api/server/stats` - Server statistics
- `GET /api/player/{username}` - Player statistics
- `GET /api/players/online` - Online players list

### Example Response
```json
{
  "success": true,
  "playerFound": true,
  "data": {
    "username": "Player123",
    "isOnline": true,
    "playtime": "45h 23m",
    "blocksBreaken": 1250,
    "blocksPlaced": 2890,
    "deaths": 12,
    "liveData": {
      "health": 18.5,
      "foodLevel": 19,
      "experienceLevel": 30,
      "location": {
        "x": 125.5,
        "y": 64.0,
        "z": -89.2,
        "world": "world"
      }
    }
  }
}
```

## 🎮 Commands

- `/stats [player]` - View player statistics
- `/statsreload` - Reload plugin configuration (requires admin permission)

## 🔒 Permissions

- `lowkeycraft.stats.view` - View statistics (default: true)
- `lowkeycraft.stats.admin` - Admin commands (default: op)

## 🌐 Website Integration

Replace your current API calls with:

```javascript
// Get player stats
const response = await fetch('http://your-server:8080/api/player/username');
const data = await response.json();

// Get server stats
const serverResponse = await fetch('http://your-server:8080/api/server/stats');
const serverData = await serverResponse.json();
```

## 🏗️ Building from Source

```bash
git clone https://github.com/yourusername/LowkeycraftStats.git
cd LowkeycraftStats
mvn clean package
```

The compiled JAR will be in `target/lowkeycraft-stats-1.0.0.jar`

## 📋 Requirements

- **Minecraft:** 1.20.4+ (Spigot/Paper)
- **Java:** 17+
- **Memory:** Minimal impact (<10MB RAM)

## 🐛 Issues & Support

If you encounter any issues:
1. Check the [Issues](../../issues) page
2. Enable debug mode in config.yml
3. Check server logs for errors

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Made for Lowkeycraft servers** 🎮