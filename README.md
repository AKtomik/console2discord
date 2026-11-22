# Console2Discord

**Monitor your Minecraft server console logs live from a Discord channel.** Forwarded logs are directly read from the console logger (log4j) and sent to Discord using webhooks. Logs are colored using ANSI!

Console2Discord is also a great way to **keep a history of your logs** in a Discord channel. Any message sent to Discord is saved with no date limit, and you can search back to a specific date using keywords with Discord search.

<img width="1530" height="536" alt="Minecraft colored console logs within Discord" src="https://github.com/user-attachments/assets/dfb0405f-d7df-4cd9-b81b-d74ad12a9be5" />

This is very [easy to set up](#setup), no need to create a bot or anything similar! Compatible with Spigot / Bukkit / Paper / Folia from **1.8 to 26.3+**. See the [compatibility table](#compatibility-table) for more details.

## Setup

In Discord:

0. You'll need a Discord server with "Manage Channels" and "Manage Webhooks" permissions (or admin)
1. Create a private channel specific for these server logs (to not spam another channel)
2. Create a webhook in this channel `Channel Settings > Integrations > New Webhook`
3. Copy the webhook URL as you'll need it just after `Copy Webhook URL`

<img width="1380" height="816" alt="Discord webhook creation tuto gif" src="https://github.com/user-attachments/assets/32eb75a3-9ca7-426c-aaa1-147b772d4aa4" />

In your Minecraft server:

4. Download the latest version of Console2Discord to `plugins/`
5. Start / Restart your server
6. Link the plugin to your webhook using `/console2discord webhook <webhook_url>`
7. **That's it!** You should see the "Hello!" message in your Discord channel.

> - Logs can contain sensitive data, keep the channel’s access private
> - Webhook URL must stay private, do not communicate your full `config.yml`

## Compatibility Table

| version | compatibility |
|---|---|
| > 26.3 | expected to work |
| 26.1 to 26.3 | **tested**|
| 1.17 to 1.21.11 | **tested**|
| 1.8 to 1.16.5 | **works** using java 16|
| < 1.8 | not tested|

**Tested with Paper, Bukkit, Spigot and Folia.** Should work with most Spigot / Paper forks.

The plugin relies on log4j2, which has been stable across versions. If a release breaks it, I’ll update the table. 

Feel free to open an [issue](https://github.com/AKtomik/console2discord/issues) if you have any compatibility issue.

## Configuration

You can find the configuration file at `plugins/console2discord/config.yml`. If you change the configuration during runtime, you can use `/console2discord reload` to apply it without needing to restart.
Some cool settings examples:

- `webhook-url`
  change webhook URL directly in config (is updated by the command)
- `content.format`
  how messages are formatted (code block, plain text, ANSI...)
- `content.stack-trace.allow`
  do forward stack trace in Discord
- `queue.wait-capacity` 
  how many log messages are kept in memory when not yet sent
- ...

You'll have a bunch of other settings to play with. All settings have a proper description commented directly in the file. Default values are great for most servers.

<img width="669" height="428" alt="config.yml content preview" src="https://github.com/user-attachments/assets/28c72b58-d5a0-47b5-989d-0e198eb17771" />

## Commands

- `/console2discord help`
  get this help message
- `/console2discord webhook <url>`
  set the webhook url
- `/console2discord status`
  show current communication state
- `/console2discord reload`
  reload the config and try establish communication back
- `/console2discord stop`
  stop the communication of logs to discord
- `/console2discord start`
  start back the communication

Command permission = `console2discord.command` (default: op)

Command alias = `c2d`

## Features List

- **real-time forwarding of your console logs**
- **forwarded to Discord using webhooks**
- use of ANSI to have colored logs in Discord
- optional stack trace forwarding
- statistics on the number of logs sent
- log format exactly like in your console
- rate limit handling using grouping, retry and wait times
- filter log messages by level
- command to change your webhook URL at runtime
- command to reload your config from file at runtime
- command to start / stop log forwarding
- config file with a lot to tweak
- maybe other things that I forgot to list here

Privacy note: The plugin only contacts the webhook URL you configure. No telemetry, no downloads.

Feel free to open an [issue](https://github.com/AKtomik/console2discord/issues) if you have any suggestion!

## Known Limitations

Here are some known limitations about this plugin. Most of them can't be easily solved, and you'll have to work with them for now.

- You can't send commands to the console from Discord using this plugin
- A Discord message can't be longer than 2000 characters
- Discord rate limits may slow down log forwarding if there are too many logs
- Below 1.17, you need to run your server with java 16 (or superior) for the plugin to work

Feel free to open an [issue](https://github.com/AKtomik/console2discord/issues) if you have any feedback, or if you encounter a bug / limitation not listed here!
