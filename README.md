# Veyra 1.0

Veyra is a Discord bot developed using Java and the JDA (Java Discord API) library. It is designed to provide server management tools, automated moderation features, and basic utility functions for Discord communities.

## Features

The bot includes several modules designed for different aspects of server management:

* **Moderation**: Includes standard administrative tools such as kick, ban, timeout, and message purging.
* **Utility**: Commands to retrieve server information, user profiles, and member counts.
* **Automation**: Handles automated tasks including assigning roles to new members, logging deleted messages, and sending welcome or leave notifications.
* **Engagement**: A dynamic status system that cycles through various custom messages to keep the bot's profile active.

## Technical Requirements

To run this project, you will need:

* **Java Development Kit (JDK) 25**: This project is built using the latest features of JDK 25.
* **Maven**: Used for dependency management.
* **A Discord Bot Token**: Required from the Discord Developer Portal.

## Installation and Setup

If you are looking to run this code yourself, follow these steps:

1. **Clone the project**: Download the source code to your local machine.
2. **Configure credentials**: Create a file named `config.properties` in the root directory. This file is ignored by the version control system to keep your keys private.
3. **Add your keys**: Inside `config.properties`, add the following lines:
    * `token=your_discord_bot_token_here`
    * `guild_id=your_target_server_id_here`
4. **Enable Intents**: In the Discord Developer Portal, ensure that Presence Intent, Server Members Intent, and Message Content Intent are toggled on.
5. **Run the application**: Open the project in IntelliJ IDEA and run the `Main` class. The bot is hosted directly through the IDE.

## Project Structure

* **Main.java**: The entry point of the application which initializes the JDA instance and the status cycle.
* **CommandManager.java**: Handles the registration and execution of slash commands.
* **Listeners**: Specialized classes that watch for specific Discord events like members joining or messages being edited.

## License

This project is open-source and released under the MIT License.

## Developer

Developed by elysety.
