# Kite Algo Trading Application

A comprehensive algorithmic trading application built with Java Spring Boot and Kite Connect integration, designed for spot trading with automatic detection of sudden price movements.

## 🚀 Features

- **Real-time Market Data**: Live quotes and historical data via Kite Connect API
- **Order Management**: Place, modify, and cancel orders with risk management
- **Sudden Movement Strategy**: Automated detection of sudden price hikes and downfalls
- **Backtesting Engine**: Test strategies on historical data with detailed metrics
- **Web Dashboard**: Interactive interface for monitoring and control
- **PostgreSQL Persistence**: Store all trading data and strategies
- **Paper Trading Mode**: Test strategies without real money
- **Technical Indicators**: RSI, MACD, and volume analysis
- **Automated Execution**: Scheduled strategy execution with configurable intervals

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Kite Connect API credentials (for live trading)

## 🛠️ Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd kite-algo-trading
```

### 2. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE kite_trading;
```

### 3. Configure Application

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/kite_trading
spring.datasource.username=your_username
spring.datasource.password=your_password

# Kite Connect Configuration (optional - for live trading)
kite.api.key=your_api_key
kite.api.secret=your_api_secret

# Trading Configuration
trading.paper-trading=true
trading.default-quantity=1
trading.risk.percentage=2.0

# Strategy Configuration
strategy.sudden-movement.threshold=2.0
strategy.sudden-movement.timeframe=5
strategy.execution.interval=60000
```

### 4. Build the Application

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 🌐 Web Dashboard

Access the interactive dashboard at: `http://localhost:8080/index.html`

### Dashboard Features:

- **Overview Panel**: Real-time statistics (orders, trades, P&L, win rate)
- **Recent Orders**: View latest orders with status
- **Recent Trades**: Monitor trade performance
- **Quick Order**: Place orders directly from the dashboard
- **Strategy Status**: View and execute active strategies
- **Backtesting**: Run strategy backtests with historical data

## 🔌 API Endpoints

### Order Management

- `POST /api/orders` - Place a new order
- `PUT /api/orders/{orderId}` - Modify an existing order
- `DELETE /api/orders/{orderId}` - Cancel an order
- `GET /api/orders/{orderId}` - Get order status
- `GET /api/orders` - Get all orders
- `GET /api/orders/positions` - Get current positions
- `GET /api/orders/holdings` - Get holdings

### Market Data

- `GET /api/market/quote/{symbol}` - Get live quote
- `GET /api/market/quotes?symbols=symbol1,symbol2` - Get multiple quotes
- `GET /api/market/historical/{symbol}` - Get historical data
- `GET /api/market/price/{symbol}` - Get current price
- `GET /api/market/change/{symbol}` - Get day change
- `GET /api/market/change-percent/{symbol}` - Get day change percentage

### Strategy Management

- `POST /api/strategies` - Create a new strategy
- `GET /api/strategies` - Get all strategies
- `GET /api/strategies/active` - Get active strategies
- `PUT /api/strategies/{strategyId}` - Update strategy
- `PUT /api/strategies/{strategyId}/activate` - Activate strategy
- `PUT /api/strategies/{strategyId}/deactivate` - Deactivate strategy
- `POST /api/strategies/{strategyId}/execute` - Execute strategy

### Backtesting

- `POST /api/backtest/run` - Run backtest
- `GET /api/backtest/run` - Run backtest with query parameters

### Dashboard

- `GET /api/dashboard/overview` - Get dashboard overview
- `GET /api/dashboard/recent-orders` - Get recent orders
- `GET /api/dashboard/recent-trades` - Get recent trades
- `GET /api/dashboard/open-positions` - Get open positions
- `GET /api/dashboard/performance` - Get performance metrics

## 📊 Sudden Movement Strategy

The application includes a built-in strategy for detecting sudden price movements:

### Strategy Logic:

1. **Movement Detection**: Identifies price movements > threshold (default 2%)
2. **Volume Analysis**: Confirms movements with volume spikes
3. **Technical Indicators**: Uses RSI and MACD for signal confirmation
4. **Risk Management**: Automatic stop-loss and take-profit levels
5. **Signal Generation**: BUY/SELL signals based on combined analysis

### Configuration:

```properties
strategy.sudden-movement.threshold=2.0    # Percentage threshold
strategy.sudden-movement.timeframe=5       # Timeframe in minutes
strategy.execution.interval=60000         # Execution interval in ms
```

## 🧪 Backtesting

Run backtests to evaluate strategy performance:

### Using API:

```bash
curl -X POST http://localhost:8080/api/backtest/run \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "RELIANCE",
    "strategyName": "Sudden Movement",
    "startDate": "2024-01-01T00:00:00",
    "endDate": "2024-01-31T23:59:59",
    "initialCapital": 100000
  }'
```

### Using Dashboard:

1. Navigate to the Backtest section
2. Enter symbol and number of days
3. Click "Run Backtest"
4. View detailed results including:
   - Total return
   - Win rate
   - Maximum drawdown
   - Sharpe ratio
   - Average trade duration

## 🔒 Security

The application includes:

- CORS configuration for frontend access
- Paper trading mode for testing
- Order validation and risk checks
- Secure API endpoints

## 📁 Project Structure

```
kite-algo-trading/
├── src/main/java/com/kite/trading/
│   ├── config/           # Configuration classes
│   ├── controller/       # REST API controllers
│   ├── service/          # Business logic services
│   ├── model/            # Database entities
│   ├── repository/       # JPA repositories
│   ├── strategy/         # Trading strategies
│   └── dto/              # Data transfer objects
├── src/main/resources/
│   ├── static/           # Web dashboard
│   └── application.properties
└── pom.xml               # Maven dependencies
```

## 🧭 Configuration Options

### Application Properties:

- `server.port`: Server port (default: 8080)
- `spring.datasource.*`: Database configuration
- `kite.api.*`: Kite Connect API credentials
- `trading.*`: Trading settings
- `strategy.*`: Strategy parameters

### Environment Variables:

Create a `.env` file in the project root:

```env
KITE_API_KEY=your_api_key
KITE_API_SECRET=your_api_secret
DB_HOST=localhost
DB_PORT=5432
DB_NAME=kite_trading
DB_USER=postgres
DB_PASSWORD=postgres
PAPER_TRADING=true
```

## 🚦 Getting Started

### 1. Start the Application

```bash
mvn spring-boot:run
```

### 2. Access the Dashboard

Open `http://localhost:8080/index.html` in your browser

### 3. Configure Kite Connect (Optional)

For live trading, configure your Kite Connect credentials in `application.properties`

### 4. Create a Strategy

Use the API or dashboard to create and activate strategies

### 5. Monitor Performance

Watch the dashboard for real-time updates and performance metrics

## 📈 Performance Metrics

The application tracks:

- **Total Return**: Overall profit/loss percentage
- **Win Rate**: Percentage of winning trades
- **Maximum Drawdown**: Largest peak-to-trough decline
- **Sharpe Ratio**: Risk-adjusted return measure
- **Average Trade Duration**: Typical holding period
- **Profit Factor**: Ratio of gross wins to gross losses

## 🛡️ Risk Management

Built-in risk controls:

- **Paper Trading Mode**: Test without real money
- **Position Sizing**: Configurable default quantity
- **Stop Loss**: Automatic loss limiting
- **Take Profit**: Automatic profit booking
- **Order Validation**: Pre-trade risk checks

## 🐛 Troubleshooting

### Database Connection Issues

```bash
# Check PostgreSQL is running
pg_isready

# Verify database exists
psql -l | grep kite_trading
```

### Port Already in Use

Change the port in `application.properties`:

```properties
server.port=8081
```

### Kite Connect Issues

- Verify API credentials are correct
- Check API rate limits
- Ensure request token is valid

## 📝 License

This project is for educational and demonstration purposes.

## ⚠️ Disclaimer

This software is for educational purposes only. Algorithmic trading involves significant risk of loss. Always test strategies thoroughly with paper trading before using real money. The authors are not responsible for any financial losses incurred through the use of this software.

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📞 Support

For issues and questions:
- Create an issue on GitHub
- Check existing documentation
- Review API endpoint documentation

## 🔄 Updates

The application includes scheduled tasks for:
- Live quote fetching (every 60 seconds)
- Strategy execution (configurable interval)
- Position monitoring (every 30 seconds)

## 🎯 Future Enhancements

- [ ] Additional technical indicators
- [ ] Machine learning integration
- [ ] Multi-strategy support
- [ ] Advanced risk management
- [ ] Mobile app support
- [ ] Real-time notifications
- [ ] Portfolio optimization
- [ ] Social trading features

---

**Happy Trading! 📈**
