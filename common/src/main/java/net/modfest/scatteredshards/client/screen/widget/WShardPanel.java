package net.modfest.scatteredshards.client.screen.widget;

import java.util.List;
import java.util.Random;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Either;

import org.apache.commons.lang3.RandomStringUtils;
import vendor.cn.zbx1425.scatteredshards.cotton.gui.client.ScreenDrawing;
import vendor.cn.zbx1425.scatteredshards.cotton.gui.widget.WPlainPanel;
import vendor.cn.zbx1425.scatteredshards.cotton.gui.widget.WSprite;
import vendor.cn.zbx1425.scatteredshards.cotton.gui.widget.WWidget;
import vendor.cn.zbx1425.scatteredshards.cotton.gui.widget.data.HorizontalAlignment;
import vendor.cn.zbx1425.scatteredshards.cotton.gui.widget.data.Insets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.client.ScatteredShardsClient;
import net.modfest.scatteredshards.client.screen.widget.scalable.WScaledLabel;
import net.modfest.scatteredshards.client.screen.widget.scalable.WScaledText;
import net.modfest.scatteredshards.client.screen.widget.scalable.WShardIcon;

public class WShardPanel extends WPlainPanel {
	
	public static final IntSupplier WHITE = () -> 0xFFFFFF;
	public static final Style HINT_STYLE = Style.EMPTY.withFont(Identifier.of("minecraft:alt"));

	private Shard shard = Shard.MISSING_SHARD.copy();
	private ShardType shardType;
	private boolean isHidden = false;
	private Text hideText = Text.translatable("gui.scattered_shards.tablet.click_on_a_shard");
	
	private final WDynamicSprite backing = new WDynamicSprite(() -> ShardType.getFrontTexture(shard.shardTypeId()));
	private final WShardIcon icon = new WShardIcon(2.0f);
	private final WScaledLabel name = new WScaledLabel(() -> shard.name(), 1.4f)
			.setShadow(true)
			.setHorizontalAlignment(HorizontalAlignment.CENTER);
	private final WScaledLabel typeDescription = new WScaledLabel(() -> ShardType.getDescription(shard.shardTypeId()), 0.5f)
			.setShadow(true)
			.setHorizontalAlignment(HorizontalAlignment.CENTER)
			.setColor(() -> shardType.textColor());
	private final WScaledLabel source = new WScaledLabel(shard::source, 0.9f)
			.setShadow(true)
			.setHorizontalAlignment(HorizontalAlignment.CENTER);
	private final WScaledText lore = new WScaledText(shard::lore, 0.8f)
			.setShadow(true)
			.setHorizontalAlignment(HorizontalAlignment.CENTER);
	private final WScaledText hint = new WScaledText(shard::hint, 0.8f)
			.setShadow(true)
			.setHorizontalAlignment(HorizontalAlignment.CENTER);

	/**
	 * Sets the shardType displayed to a static value. Note: Prevents the shardType from being updated if the configured shard is mutated!
	 */
	public WShardPanel setType(Identifier shardTypeId, ShardType value) {
		this.shardType = value;
		backing.setImage(ShardType.getFrontTexture(shardTypeId));
		typeDescription.setText(ShardType.getDescription(shardTypeId));
		typeDescription.setColor(value::textColor);
		return this;
	}

	/**
	 * Sets the icon displayed to a static value. Note: Prevents shard icon from being updated if the configured shard is mutated!
	 */
	public WShardPanel setIcon(Either<ItemStack, Identifier> icon) {
		this.icon.setIcon(icon);
		return this;
	}

	public WShardPanel setName(Supplier<Text> text, IntSupplier color) {
		this.name.setText(text);
		this.name.setColor(color);
		return this;
	}

	public WShardPanel setSource(Supplier<Text> text, IntSupplier color) {
		this.source.setText(text);
		this.source.setColor(color);
		return this;
	}

	public WShardPanel setLore(Supplier<Text> text, IntSupplier color) {
		this.lore.setText(() -> text.get().copy().formatted(Formatting.ITALIC));
		this.lore.setColor(color);
		return this;
	}

	public WShardPanel setHint(Supplier<Text> text, IntSupplier color) {
		String rawText = text.get().getString();
		Random random = new Random(rawText.hashCode());
		StringBuilder randomText = new StringBuilder();
		for (int i = 0; i < rawText.length(); i++) {
			char c = rawText.charAt(i);
			if (c == ' ') {
				randomText.append(' ');
			} else if (c == '，') {
				randomText.append(", ");
			} else if (c == '。') {
				randomText.append(". ");
			} else if (c < 128) {
				randomText.append(RandomStringUtils.random(1, 0, 0, true, false, null, random));
			} else {
				randomText.append(RandomStringUtils.random(2, 0, 0, true, false, null, random));
			}
		}
		this.hint.setText(() -> Text.literal(randomText.toString()).fillStyle(HINT_STYLE));
		this.hint.setColor(color);
		this.hint.setHover(text);
		return this;
	}

	public WShardPanel setShard(Shard shard) {
		this.shard = shard;
		this.isHidden = false;

		setType(shard.shardTypeId(), ScatteredShardsAPI.getClientLibrary().shardTypes().get(shard.shardTypeId()).orElse(ShardType.MISSING));
		icon.setIcon(shard::icon);
		setName(shard::name, WHITE);
		setSource(shard::source, WHITE);
		setLore(shard::lore, WHITE);
		setHint(shard::hint, WHITE);

		return this;
	}

	public WShardPanel setHidden(boolean hidden) {
		this.isHidden = true;
		return this;
	}
	
	public WShardPanel hideWithMessage(Text message) {
		this.isHidden = true;
		this.hideText = message;
		return this;
	}
	
	private int getLayoutWidth() {
		return this.getWidth() - insets.left() - insets.right();
	}

	public WShardPanel() {
		this.shardType = ShardType.MISSING;
		this.width = 124;
		this.height = 200;
		this.setInsets(Insets.ROOT_PANEL);
		
		add(name, 0, 0, getLayoutWidth(), 18);
		add(typeDescription, 0, 16, getLayoutWidth(), 16);
		add(source, 0, 25, getLayoutWidth(), 16);
		
		int cardScale = 2;
		int cardX = ((this.getLayoutWidth()) / 2) - (12 * cardScale);
		add(backing, cardX, 40, 24*cardScale, 32*cardScale);
		
		add(icon, cardX + (4 * cardScale), 40 + (ScatteredShardsClient.ICON_Y_OFFSET * cardScale), 16 * cardScale, 16 * cardScale);

		
		add(lore, 0, 113, getLayoutWidth(), 32);
		
		//TODO: Add divider image
		add(new WSprite(ScatteredShards.id("textures/gui/divider.png")), cardX, 145, 24 * cardScale, 1);
		
		add(hint, 0, 149, getLayoutWidth(), 32);
	}
	
	@Override
	public void layout() {
		// We are already perfectly laid out from the constructor.
	}
	
	@Override
	protected void expandToFit(WWidget w, Insets insets) {
		// Do not expand to fit anything.
		return;
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
		if (!isHidden) {
			super.paint(context, x, y, mouseX, mouseY);
			return;
		}
		
		var backgroundPainter = this.getBackgroundPainter();
		if (backgroundPainter!=null) backgroundPainter.paintBackground(context, x, y, this);
		
		@SuppressWarnings("resource")
		var textRenderer = MinecraftClient.getInstance().textRenderer;
		List<OrderedText> lines = textRenderer.wrapLines(hideText, 108);
		int yOffset = 30;
		int layoutWidth = this.getWidth() - this.getInsets().left() - this.getInsets().right();
		for(OrderedText t : lines) {
			ScreenDrawing.drawStringWithShadow(context, t, HorizontalAlignment.CENTER, x + this.insets.left(), y + yOffset, layoutWidth, 0xFF_FFFFFF);
			yOffset += textRenderer.fontHeight;
		}
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void addPainters() {
		this.setBackgroundPainter((context, left, top, panel) -> {
			context.setShaderColor(1, 1, 1, 1);
			ScreenDrawing.drawGuiPanel(context, left, top, panel.getWidth(), panel.getHeight());
			ScreenDrawing.drawBeveledPanel(context, left+4, top+4, panel.getWidth()-8, panel.getHeight()-8);
			context.fillGradient(left+5, top+5, left+5+panel.getWidth()-10, top+5+panel.getHeight()-10, ScatteredShardsClient.RIGHT_TOP, ScatteredShardsClient.RIGHT_BOTTOM);
		});
	}

	public WShardPanel(Shard shard) {
		setShard(shard);
	}

	public Shard getShard() {
		return shard;
	}
	
	@Override
	public boolean canResize() {
		return false;
	}
}
