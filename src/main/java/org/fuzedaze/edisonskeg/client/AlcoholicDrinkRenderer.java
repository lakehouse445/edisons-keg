package org.fuzedaze.edisonskeg.client;

import org.fuzedaze.edisonskeg.alcohol.AlcoholType;
import org.fuzedaze.edisonskeg.item.AlcoholicDrinkItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class AlcoholicDrinkRenderer extends GeoItemRenderer<AlcoholicDrinkItem> {
    public AlcoholicDrinkRenderer(AlcoholType type) {
        super(new AlcoholicDrinkModel(type));
    }
}
