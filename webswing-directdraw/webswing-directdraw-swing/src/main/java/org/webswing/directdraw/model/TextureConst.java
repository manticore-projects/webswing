package org.webswing.directdraw.model;

import java.awt.TexturePaint;

import org.webswing.directdraw.DirectDraw;
import org.webswing.directdraw.proto.Directdraw.TextureProto;

public class TextureConst extends MutableDrawConstantHolder<TexturePaint, TextureProto> {

  public TextureConst(DirectDraw context, TexturePaint value) {
    super(context, value);
  }

  @Override
  public String getFieldName() {
    return "texture";
  }

  @Override
  public TextureProto buildMessage(TexturePaint value) {
    TextureProto.Builder model = TextureProto.newBuilder();
    model.setImage(new ImageConst(getContext(), value.getImage()).toMessage());
    model.setAnchor(new RectangleConst(getContext(), value.getAnchorRect()).toMessage());
    return model.build();
  }

  @Override
  public TexturePaint getValue() {
    return new TexturePaint(ImageConst.getValue(message.getImage()),
        RectangleConst.getValue(message.getAnchor()));
  }
}
