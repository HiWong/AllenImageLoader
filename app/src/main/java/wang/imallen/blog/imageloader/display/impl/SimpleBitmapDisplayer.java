/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package wang.imallen.blog.imageloader.display.impl;

import android.graphics.Bitmap;

import wang.imallen.blog.imageloader.constant.LoadedFromType;
import wang.imallen.blog.imageloader.display.BitmapDisplayer;
import wang.imallen.blog.imageloader.wrap.ViewWrapper;

/**
 * Just displays {@link android.graphics.Bitmap} in
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.5.6
 */
public final class SimpleBitmapDisplayer implements BitmapDisplayer {
	@Override
	public void display(Bitmap bitmap, ViewWrapper viewWrapper, LoadedFromType loadedFromType) {
		viewWrapper.setImageBitmap(bitmap);
	}
}