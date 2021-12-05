package newconsole.ui;

import arc.*;
import arc.func.*;
import arc.struct.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.utils.*;
import mindustry.gen.*;
import mindustry.ui.*;

import newconsole.ui.*;

/** Inherited from my previous mod, newcontrols. */
public class Spinner extends TextButton {
	
	public static Collapser lastCollapser;
	
	public Collapser col;
	public BetterPane pane;
	public TextButton button;
	public Image image;
	
	/** Whether to remove collapser if any of ancestors are invisible / untouchable */
	public boolean autoHide = true;
	/** Whether this collapser should be hidden if another unique collapser is displayed and vice versa */
	public boolean unique = true;
	protected float collapseTime = 0.4f;
	protected float padW = 16f, padH = 16f; //padding cus else it looks ugly
	
	Timer.Task hideTask;
	
	public Spinner(String header, boolean unique, Cons<Table> constructor) {
		super(header, Styles.clearTogglet);
		this.unique = unique;
		
		//todo: wtf is this and is this necessary?
		add(image = new Image(Icon.downOpen)).size(Icon.downOpen.imageSize() * Scl.scl(1f)).padLeft(padW / 2f).left();
		getCells().reverse();
		
		col = new Collapser(base -> {
			pane = new BetterPane(t -> {
				t.left();
				constructor.get(t);
			});
			base.add(pane).growX().scrollX(false);
		}, true).setDuration(collapseTime);
		
		//todo: wtf why and why did i even do that
		clicked(() -> {
			col.toggle();
			if (col.isCollapsed()) {
				hide(true);
			} else {
				show(true);
			}
		});
		
		//todo: can i avoid that
		update(() -> {
			setChecked(!col.isCollapsed());
			image.setDrawable(!col.isCollapsed() ? Icon.upOpen : Icon.downOpen);
		});
		
		col.update(() -> {
			if (unique && lastCollapser != col) {
				hide(true);
			}
			
			if (col.getScene() != null) {
				col.visible = true;
				toFront();
				col.color.a = parentAlpha * color.a;
				col.setSize(width, col.getPrefHeight());
				
				Vec2 point = localToStageCoordinates(Tmp.v1.set(0, -col.getPrefHeight()));
				float height = Core.scene.getHeight() - point.y;
				
				if (point.y < pane.getWidget().getPrefHeight() && (point.y < Core.scene.getHeight() / 2)) {
					point = localToStageCoordinates(Tmp.v1.set(0, getPrefHeight()));
					height = point.y;
				}
				col.setPosition(point.x, point.y);
				
				pane.setHeight(Math.min(pane.getWidget().getPrefHeight(), height));
			}
			
			if (autoHide && col.getScene() != null) {
				//find any invisible or not touchable ancestors, hide if found
				Element current = this;
				while (true) {
					if (!current.visible || current.touchable == Touchable.disabled) {
						hide(false);
						break;
					}
					if (current.parent == null) break;
					current = current.parent;
				}
			}
		});
	}
	
	public Spinner(String header, Cons<Table> constructor) {
		this(header, true, constructor);
	}
	
	public void show(boolean animate) {
		if (hideTask != null) hideTask.cancel();
		
		col.setCollapsed(false, animate);
		
		Scene prevStage = col.getScene();
		if (prevStage != null) prevStage.root.removeChild(col);
		Scene stage = getScene();
		if (stage == null) return;
		stage.add(col);
		
		col.toFront();
		toFront();
		
		if (unique) {
			lastCollapser = col;
		}
	}
	
	public void hide(boolean animate) {
		col.setCollapsed(true, animate);
		Scene stage = getScene();
		
		if (stage != null) {
			if (animate) {
				hideTask = Timer.schedule(() -> {
					stage.root.removeChild(col);
					hideTask = null;
				}, collapseTime);
			} else {
				stage.root.removeChild(col);
			}
		}
	}
	
	@Override
	public float getPrefWidth() {
		return super.getPrefWidth() + padW;
	}
	
	@Override
	public float getPrefHeight() {
		return super.getPrefWidth() + padH;
	}
	
	/** Makes every unique spinner collapse */
	public static void hideAllUnique() {
		lastCollapser = null;
	}
	
}