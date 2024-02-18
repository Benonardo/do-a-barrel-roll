<img style="text-align:center" src="img/banner.png">

![Mod Loader](https://img.shields.io/badge/mod%20loader-fabric%2c%20forge-a64581?style=flat)
![Enviroment](https://img.shields.io/badge/environment-client%2c%20opt%20server-536a9e?style=flat)

Do a Barrel Roll is a cringe, fully clientside mod for Fabric that changes 
elytra flight to be more fun and semi-realistic. This version however (Do a Hacked Roll) disables annoying limitations 
by the original author!.
It achieves this by redesigning movement with a 
completely unlocked thrusting in flight.

![](img/ravine.gif)

## Controls

The default controls are as follows, but can be modified:

- W to thrust
- Mouse x axis to roll
- Mouse y axis to pitch
- strafe keys (normally A and D) to yaw

## Configuration

The mod can be configured in-game using [ModMenu](https://modrinth.com/mod/modmenu) and [YACL](https://modrinth.com/mod/yacl).
Once you install both of these,
you can access the config screen by finding the mod in the mods list and pressing the config button.

A wide range of options are available, including custom mouse behavior, elytra activation restrictions and
changing values of modifiers like banking, sensitivity and more.

## Server-side features

Visual aspects of the mod (playermodel roll in particular) can be synced between 
clients by installing it on the server-side as well. 
Everything is still fully compatible with both vanilla clients and servers. 
All the following configurations are valid:

- Server with mod, client with mod: visuals are synced
- Server with mod, client without mod: client can join and play without issues, but can't see visuals
- Server without mod, client with mod: client can join and play without issues, but can only see their own visuals
- Server with mod, client 1 with mod, client 2 with mod, client 3 without mod: client 1 and 2 can see each other's visuals, client 3 cannot
- Server without mod, client 1 with mod, client 2 with mod, client 3 without mod: client 1 and 2 can only see their own visuals

## Credits

Based on the [Cool Elytra Roll](https://github.com/Jorbon/cool_elytra) mod by [Jorbon](https://github.com/Jorbon),
specifically it's "realistic mode".

Originally ported to Forge by [MehVahdJukaar](https://github.com/MehVahdJukaar).

Mod icon by Mizeno.

Original (lame) version by enjarai.

## For cheater friends

### Thrusting

The mod includes a "thrusting" feature that is probably considered cheating by most servers, 
so it's definitely enabled!

### Other things to look aut fpr

The server config includes a few other features to configure server-side lameness, including:

- `forceEnabled`: Forces the mod to be enabled for all players, regardless of their client configuration. **This should
not be a big issue**
- `forceInstalled`: Rejects any player trying to join without having the mod installed on their client. **This will be
removed soon**
- `installedTimeout`: The amount of time (in ticks) to wait for a client to respond to the `do_a_barrel_roll:config_sync` packet.
  - If the client does not respond in time, and `forceInstalled` is set to `true`, they will be kicked from the server.
  - You may want to increase this value if players with bad connections are getting kicked despite having the mod installed.

It is also possible to allow specific players to bypass configured restrictions by giving them level 2 operator status 
or the `do_a_barrel_roll.ignore_config` permission. **We could use this to bypass everything**