package tcs.exportallplayersid;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class tcsexportallplayersid extends JavaPlugin implements CommandExecutor {

    private Set<Pattern> sensitivePatterns;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSensitiveWords();

        this.getCommand("tcsexportallplayersid").setExecutor(this);
        getLogger().info("tcsexportallplayersid 已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("tcsexportallplayersid 已禁用！");
    }

    private void loadSensitiveWords() {
        List<String> words = getConfig().getStringList("sensitive-words");
        this.sensitivePatterns = new HashSet<>();
        for (String word : words) {
            try {
                sensitivePatterns.add(Pattern.compile(word, Pattern.CASE_INSENSITIVE));
            } catch (Exception e) {
                getLogger().warning("敏感词正则无效，已跳过: " + word);
            }
        }
        getLogger().info("已加载 " + sensitivePatterns.size() + " 个敏感词/正则。");
    }

    private boolean containsSensitiveWord(String text) {
        if (text == null) return false;
        for (Pattern pattern : sensitivePatterns) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("tcsexportallplayersid")) {
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("§a用法: /tcsexportallplayersid <export|reload>");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("tcsexportallplayersid.reload")) {
                sender.sendMessage("§c你没有权限执行此命令！");
                return true;
            }
            reloadConfig();
            loadSensitiveWords();
            sender.sendMessage("§a敏感词配置已重新加载！");
            return true;
        }

        if (args[0].equalsIgnoreCase("export")) {
            if (!sender.hasPermission("tcsexportallplayersid.export")) {
                sender.sendMessage("§c你没有权限执行此命令！");
                return true;
            }

            CompletableFuture.runAsync(() -> {
                OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
                Set<String> namesSet = new HashSet<>();
                Set<String> filteredNames = new HashSet<>();
                AtomicInteger filteredCount = new AtomicInteger(0);

                for (OfflinePlayer player : offlinePlayers) {
                    String name = player.getName();
                    if (name != null) {
                        if (!containsSensitiveWord(name)) {
                            namesSet.add(name);
                        } else {
                            filteredCount.incrementAndGet();
                            filteredNames.add(name);
                        }
                    }
                }

                int totalPlayers = namesSet.size();

                StringBuilder sb = new StringBuilder();
                int count = 0;
                for (String name : namesSet) {
                    sb.append(name);
                    count++;
                    if (count != namesSet.size()) sb.append(",");
                }

                File folder = new File(getDataFolder().getAbsolutePath());
                if (!folder.exists()) folder.mkdirs();

                File file = new File(folder, "players.csv");
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(sb.toString());
                } catch (IOException e) {
                    sender.sendMessage("写入玩家文件时出错: " + e.getMessage());
                    e.printStackTrace();
                }

                File filteredFile = new File(folder, "filtered_players.csv");
                try (FileWriter writer = new FileWriter(filteredFile)) {
                    count = 0;
                    for (String name : filteredNames) {
                        writer.write(name);
                        count++;
                        if (count != filteredNames.size()) writer.write(",");
                    }
                } catch (IOException e) {
                    getLogger().warning("写入被过滤玩家文件时出错: " + e.getMessage());
                }

                Bukkit.getScheduler().runTask(this, () -> {
                    sender.sendMessage("玩家 ID 导出完成（敏感词已过滤，重复已去掉），共 " + totalPlayers + " 个玩家。");
                    sender.sendMessage("共过滤掉 " + filteredCount.get() + " 个敏感玩家名，详情请查看 filtered_players.csv");
                });
            });

            return true;
        }

        sender.sendMessage("§c未知子命令: " + args[0]);
        return true;
    }
}
