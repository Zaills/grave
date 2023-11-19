package net.zaills.grave.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

@Modmenu(modId = "grave")
@Config(name = "grave-config", wrapperName = "GraveConfig")
public class GraveConfigModel {
	public boolean Priorities_Inv = false;
}
