# StrengthSMP Plugin Testing Guide

This guide provides comprehensive testing instructions for all features of the StrengthSMP plugin.

## Prerequisites
- Minecraft server running Paper 1.21+
- Plugin installed in plugins folder
- At least 2 players online for testing interactions
- Discord bot configured for announcements (optional)

## Commands Testing

### Basic Commands
1. **Check Strength**
   - Command: `/strength`
   - Expected: Shows your current strength points
   - Test: Run as any player

2. **Withdraw Strength**
   - Command: `/withdraw <amount>`
   - Expected: Gives strength item, deducts from total
   - Test: `/withdraw 5` - should give 5 strength items

3. **Check Assigned Weapon**
   - Command: `/ability`
   - Expected: Shows your assigned weapon type
   - Test: Run as any player (may show none if not assigned)

### Bounty System
4. **Place Bounty**
   - Command: `/bounty <player> <amount>`
   - Expected: Deducts strength, broadcasts bounty, target glows permanently
   - Test: `/bounty PlayerName 10` - target should glow white outline

5. **List Bounties**
   - Command: `/bounty list`
   - Expected: Shows active bounties
   - Test: After placing bounty, check the list

### Soulbind System
6. **Soulbind Item**
   - Command: `/soulbind` (hold item in main hand)
   - Expected: Adds "SOULBOUND" lore, makes item unbreakable, costs 3 strength for new, 1 for transfer
   - Test: Hold a sword, run `/soulbind` - item should become unbreakable and keep on death

### Admin Commands
7. **Admin Panel**
   - Command: `/admin`
   - Expected: Opens admin GUI with various options
   - Test: Run as OP player

8. **Send Announcement**
   - Command: `/announce`
   - Expected: Sends plugin info to Discord in multiple parts
   - Test: Requires Discord bot setup, check Discord channel

## Weapon Powers Testing

### Preparation
- Assign weapons using admin GUI or commands
- Ensure players have correct weapon types assigned
- Test in survival mode with appropriate weapons

### Sword Powers (Assigned: Sword)
9. **Combo Damage Multiplier**
   - Action: Hit enemy 3 times in quick succession with diamond sword
   - Expected: 4th hit deals 1.5x damage, shows multiplier message, particles
   - Test: Attack dummy/player repeatedly

10. **Shockwave**
    - Action: Complete combo (3 hits)
    - Expected: Damages nearby enemies in 4-block radius, knocks back, particles
    - Test: Combo near multiple enemies

### Bow Powers (Assigned: Bow)
11. **Sonic Beam**
    - Action: Shoot arrow with bow
    - Expected: Arrow explodes on impact, damages in 3-block radius, sets fire, increased damage
    - Test: Shoot at target, check explosion and fire effect

### Axe Powers (Assigned: Axe)
12. **Stun and Bleed**
    - Action: Hit enemy with axe
    - Expected: Chance to stun (particles), bleed damage over time (2 damage after 2 seconds)
    - Test: Attack enemy, check for stun message and delayed damage

### Crossbow Powers (Assigned: Crossbow)
13. **Chain Leash**
    - Action: Shoot enemy with crossbow
    - Expected: Pulls nearby enemies toward target, particles, messages
    - Test: Shoot at enemy with others nearby

### Trident Powers (Assigned: Trident)
14. **Wave Riding**
    - Action: Right-click with trident while in water
    - Expected: Speed III + Dolphins Grace for 5 seconds, 15-second cooldown
    - Test: Use in water, check speed boost and cooldown message

### Shield Powers (Assigned: Shield)
15. **Block Counter**
    - Action: Block attack with shield
    - Expected: Speed II for 5 seconds, magical particles, counterattack if close (2 damage + knockback)
    - Test: Block incoming attack, check effects

## Death and Combat Testing

### Death Mechanics
16. **Strength Transfer**
    - Action: Kill player with strength
    - Expected: Victim loses 1 strength, attacker gains item drop, death certificate drop
    - Test: Kill player, check drops and strength changes

17. **Bounty Claim**
    - Action: Kill player with bounty
    - Expected: Attacker gains bounty amount as strength items, broadcast message
    - Test: Kill bountied player, check extra drops

18. **Soulbound Keep Inventory**
    - Action: Die with soulbound item
    - Expected: Soulbound items stay in inventory, unbreakable
    - Test: Die with soulbound item, check inventory after respawn

### Anti-Farm System
19. **Kill Limits**
    - Action: Kill same player multiple times
    - Expected: After 2 kills in 10 minutes, further kills give no strength
    - Test: Kill same player 3+ times quickly

## Discord Integration Testing

### Announcements
20. **Plugin Info Broadcast**
    - Action: Run `/announce`
    - Expected: Multiple messages sent to Discord with ranks, systems, etc.
    - Test: Check configured Discord channel

### Kill Logging
21. **Death Notifications**
    - Action: Player death
    - Expected: Kill logged to Discord with details
    - Test: Check Discord for kill messages

### Leaderboards
22. **Automatic Updates**
    - Action: Player deaths and strength changes
    - Expected: Leaderboard updates in Discord
    - Test: Check Discord embeds for current stats

## Edge Cases and Error Testing

### Error Conditions
23. **Insufficient Strength**
    - Commands: `/withdraw`, `/bounty`, `/soulbind` with low strength
    - Expected: Error messages
    - Test: Try commands with 0 strength

24. **Invalid Targets**
    - Commands: `/bounty` with offline player
    - Expected: "Player not found" message
    - Test: Target non-existent player

25. **Wrong Weapon Types**
    - Action: Use weapon abilities without correct assignment
    - Expected: No effects trigger
    - Test: Unassigned player using weapons

26. **Riptide Conflicts**
    - Action: Trident with Riptide enchantment
    - Expected: Ability blocked, warning message
    - Test: Enchant trident with Riptide, try ability

## Performance Testing

27. **Multiple Players**
    - Action: 10+ players using abilities simultaneously
    - Expected: No server lag, all effects work
    - Test: Stress test with many players

28. **Long-term Effects**
    - Action: Leave bounties active for 24+ hours
    - Expected: Automatic glow removal
    - Test: Check glow persistence

## Troubleshooting

- If abilities don't trigger: Check weapon assignment with `/ability`
- If Discord doesn't work: Verify bot token and channel ID in config
- If builds fail: Check for compilation errors in console
- If particles missing: Some effects use commented particles due to version compatibility

## Test Checklist
- [ ] All commands respond correctly
- [ ] All weapon powers trigger appropriately
- [ ] Death mechanics work (transfers, drops)
- [ ] Bounty system (placing, glowing, claiming)
- [ ] Soulbind (durability, keep inventory)
- [ ] Discord integration (announcements, logging)
- [ ] Error handling (invalid inputs, insufficient resources)
- [ ] Performance under load
- [ ] Edge cases and anti-abuse measures

This guide covers all plugin features. Run tests in a controlled environment and document any issues found.</content>
<parameter name="filePath">TESTING_GUIDE.md