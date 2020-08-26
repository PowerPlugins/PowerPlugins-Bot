[badge]: https://img.shields.io/badge/Join_our-Discord-7289DA?style=plastic&logo=discord&logoColor=white
[invite]: https://discord.gg/psPECvY

[image]: https://i.imgur.com/YVDGmyh.png
[examples]: https://imgur.com/a/7HtLeOW

[LICENSE]: https://github.com/PowerPlugins/PowerPlugins-Plugin/blob/master/LICENSE

# PowerPlugins Plugin
[![badge]][invite]

## Purpose
The plugin has two main functionalities which are used for the PowerPlugins Server and Discord.

### Update Notifications
The plugin informs Members on the Discord Server about new and updated plugins using Webhooks.

**Preview**:  
![image]

### Improved `/plugins` command
The `/plugins` command is altered by providing an option to list Plugins in categories and also provide more detailed information.

You can find screenshots displaying some examples [here][examples].

## Using it yourself
You want to use this plugin for your own Server? You sure can!

If you want to use it, follow this step-by-step guide to make sure that there won't be any issues.

### 1. Compile the Jar
You have to compile the jar yourself.  
We use Gradle for handling dependencies. Just clone this repository to your desktop and execute `gradlew clean shadowJar` to build a shaded, executable jar with all dependencies.  
You should find a jar called `PowerPlugins.jar` in the `/build/libs` directory.

Note that you may need to run `./gradlew clean shadowJar` on linux-based PCs.

### 2. Setup configurations
When you start-up the plugin for the first time will it do a few things first:

1. It will create a `config.yml` and a `plugins` folder inside `/plugins/PowerPlugins`
2. It will create a yaml-file for each plugin on the server with the name matching the plugin's name inside the previously created `plugins` folder.

A generated plugin file may look like this:  
```yaml
info:
  version: 1.4.3
  url: 'https://github.com/PowerPlugins/PowerPlugins-Plugin'
  name: 'PowerPlugins'
  authors:
  - 'Andre_601'
  description: ''
  category: 'private'
  depends: []
  softdepends: []
```

It is important to point out, that `depends` and `softdepends` are based on the plugin's plugin.yml file and if it has those values set or not.  
`category` can be `free`, `premium` or `private`.

You should now head over to the config.yml. By default will it look like this:  
```yaml
#
# PowerPlugins Bot
#

guild:
  #
  # The webhook URL to use for posting updates.
  webhook: ''
  #
  # The ID of the role, which would be mentioned for plugin-updates.
  role: ''
```

For `webhook` should you provide a URL to a Discord Webhook and for `role` the ID of whatever role should be pinged.  
As a final step should you also configure each plugin file (category, description, etc). Note that you should at least set a URL as that is how the plugin notices a newly added plugin from an updated one.

Now just save everything, restart your server and everything should be set up.

## Troubleshooting

### The plugin doesn't send any webhook notifications
Make sure you've actually set a valid Discord Webhook URL in the config.yml

### The Webhook doesn't ping the defined role
You need to grant the `Mention @everyone, @here, and All Roles` permission for `@everyone`.

It's recommended to set the webhook in a channel, where only selected people (Mods, Admins, etc.) can chat and `@everyone` can't.  
This prevents possible abuse of the above permission.

### The plugin constantly sends notification about newly added plugins
Make sure you've set a URL (or any text) for the plugin(s) in question.  
The plugin checks the files for if they have a URL set and if not, sees the plugin as "newly added".

## License
This code is under the MIT-License.  
Check the [LICENSE] file for more info.