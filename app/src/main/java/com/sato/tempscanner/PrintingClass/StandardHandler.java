package com.sato.tempscanner.PrintingClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class StandardHandler extends DefaultHandler {

	/**
	 * �?�在処�?�中�?�エレメント
	 */
	private ArrayList<String> mElements;
	
	/**
	 * エレメント項目�?�リスト
	 */
	private ArrayList<String> mElementItems;
	
	/**
	 * エレメント�?�値
	 */
	private StringBuilder mElementValue;
	
	/**
	 * データマップ
	 */
	private Map<String, String> mItemMap;

	
	public Map<String, String> getItem() {
		return mItemMap;
	}

	/**
	 * コンストラクタ
	 */
	public StandardHandler() {
		mElements = new ArrayList<String>();
		mElementItems = new ArrayList<String>();
		mElementValue = null;
		mItemMap = new HashMap<String, String>();
	}

	/**
	 * ドキュメント開始時
	 */
	@Override
	public void startDocument() throws SAXException {
		
		super.startDocument();
		
		mElements.clear();
	}

	/**
	 * �?素�?�開始タグ読�?�込�?�時
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		super.startElement(uri, localName, qName, attributes);
		
		// エレメント�?�リスト�?�追加
		mElements.add(localName);
		
		// エレメント�??�?�準備
		prepareElementName();
		
		// �?素�??を�?�得
		String elementName = getCurrentElementName();
		int attributeCount = attributes.getLength();
		for (int n = 0; n < attributeCount; n++) {
			String name = attributes.getLocalName(n);
			String value = attributes.getValue(n);
			mItemMap.put(elementName + ".@" + name, value);
		}
		
		mElementValue = null;
	}

	/**
	 * テキストデータ読�?�込�?�時
	 */
	@Override
	public void characters(char[] ch, int offset, int length) throws SAXException {
		
		super.characters(ch, offset, length);
		
		// エレメント内�?�文字列を作�?�?�る
		if (mElementValue == null) {
			mElementValue = new StringBuilder();
		}
		mElementValue.append(ch, offset, length);
	}

	/**
	 * �?素�?�終了タグ読�?�込�?�時
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		String elementValue;
		
		// �?��?��?��?��?��?�得�?��?�エレメント�?�値を�?�得�?�る
		if (mElementValue == null) {
			elementValue = null;
		} else {
			elementValue = mElementValue.toString();
			elementValue = StandardHandler.unescapeXmlString(elementValue, false);
		}
		
		// エレメント�?�値�?��?�得終了
		mElementValue = null;
		
		// �?素を追加�?�る
		if (elementValue != null) {
			String elementName = getCurrentElementName();
			mItemMap.put(elementName, elementValue);
		}
		
		// �?素を�?��?��?�消�?�
		mElements.remove(mElements.size() - 1);
		
		super.endElement(uri, localName, qName);
	}

	/**
	 * エレメント�??�?�準備
	 */
	private void prepareElementName() {
		
		// パラメーター�?��??�?を生�?�?�る(ルート�?素を除�??)
		String parameterName = "";
		int elementLength = mElements.size();
		for (int n = 1; n < elementLength; n++) {
			if (parameterName.length() > 0) {
				parameterName += ".";
			}
			parameterName += mElements.get(n);
		}
		
		// �?��?��??�?�?��?素�?��?�る�?�
		int sameItemCount = 0;
		for (String elementItem : mElementItems) {
			if (parameterName.equals(elementItem)) {
				sameItemCount++;
			}
		}
		
		// �?��?��??�?�?��?素�?�1�?��?��?��?�る場�?��?��?既存�?��?素�?�インデックス[0]を�?��?�る
		if (sameItemCount == 1) {
			ArrayList<String> replaceKeys = new ArrayList<String>();
			for (String key : mItemMap.keySet()) {
				if (key.equals(parameterName)) {
					replaceKeys.add(key);
				} else if (key.equals(parameterName + ".")) {
					replaceKeys.add(key);
				}
			}
			for (String replaceKey : replaceKeys) {
				String newKey = parameterName + "[0]";
				if (parameterName.length() < replaceKey.length()) {
					newKey += replaceKey.substring(parameterName.length());
				}
				String value = mItemMap.get(replaceKey);
				mItemMap.remove(replaceKey);
				mItemMap.put(newKey, value);
			}
		}
		
		// 自分自身を追加
		mElementItems.add(parameterName);
	}
	
	/**
	 * �?�在�?�エレメント�??を�?�得�?�る
	 * @return エレメント�??
	 */
	private String getCurrentElementName() {
		
		// パラメーター�?��??�?を生�?�?�る(ルート�?素を除�??)
		String parameterName = "";
		int elementLength = mElements.size();
		for (int n = 1; n < elementLength; n++) {
			if (parameterName.length() > 0) {
				parameterName += ".";
			}
			parameterName += mElements.get(n);
		}
		
		// �?��?��??�?�?��?素�?��?�る�?�
		int sameItemCount = 0;
		for (String elementItem : mElementItems) {
			if (parameterName.equals(elementItem)) {
				sameItemCount++;
			}
		}
		
		// 自分自身以外�?��?��?��??�?�?��?素�?��?�れ�?��?�?列�?��?��?�インデックスを�?��?�る
		if (sameItemCount > 1) {
			parameterName += "[" + Integer.toString(sameItemCount - 1) + "]";
		}
		
		return parameterName;
	}
	
	/**
	 * XML文字列�?�エスケープを解除�?��?��?�。(SAX�?�エレメント�?�内容�?�エスケープ�?�れ�?�文字を解除�?��?��?��??�?��?��?�返�?��?��?�。属性値�?�エスケープ�?�解除�?�れ�?��?�。)
	 * @param escapedString エスケープ�?�れ�?�文字を指定�?��?��?�。
	 * @param isAttribute 属性�?�場�?��?��?�trueを指定�?��?��?�。エレメント�?�内容�?�場�?��?�falseを指定�?��?��?�。
	 * @return エスケープを解除�?��?�文字列を返�?��?��?�。
	 */
	public static String unescapeXmlString(String escapedString, boolean isAttribute) {
		
		String value;
		
		// 文字列�?�指定�?�れ�?��?��?��?�場�?��?�何も�?��?��?�
		if (escapedString == null) {
			return null;
		} else if (escapedString.length() <= 0) {
			return "";
		}
		
		ArrayList<String> escapedCharacters = new ArrayList<String>();
		ArrayList<String> unescapedCharacters = new ArrayList<String>();
		final String lt = "&lt;";
		final String gt = "&gt;";
		final String amp = "&amp;";
		final String quot = "&quot;";
//		final String apos = "&apos;";
		
		if (escapedString.indexOf(lt) >= 0) {
			escapedCharacters.add(lt);
			unescapedCharacters.add("<");
		}
		if (escapedString.indexOf(gt) >= 0) {
			escapedCharacters.add(gt);
			unescapedCharacters.add(">");
		}
		if (escapedString.indexOf(amp) >= 0) {
			escapedCharacters.add(amp);
			unescapedCharacters.add("&");
		}
		if (isAttribute && (escapedString.indexOf(quot) >= 0)) {
			escapedCharacters.add(quot);
			unescapedCharacters.add("\"");
		}
//		if (isAttribute && (escapedString.indexOf(apos) >= 0)) {
//			escapedCharacters.add(apos);
//			unescapedCharacters.add("\'");
//		}
		
		// 置�?��?�る文字数
		int characterCount = Math.min(escapedCharacters.size(), unescapedCharacters.size());
		
		// 置�?��?�る文字�?��?��?�れ�?��?もら�?��?�文字列を�??�?��?��?�返�?�
		if (characterCount <= 0) {
			value = escapedString;
			
		// 置�?��?�る文字�?��?�る場�?��?�置�?��?�る
		} else {
			
			// 分割�?�れ�?�文字列リスト
			ArrayList<String> splitStrings = new ArrayList<String>();
			splitStrings.add(escapedString);
			
			// エスケープ�?�れ�?�文字を置�?��?�る
			for (int characterIndex = 0; characterIndex < characterCount; characterIndex++) {
				
				// 分割�?�れ�?�文字列を�?��?��?�処�?��?�る
				ArrayList<String> splitStringBuffer = new ArrayList<String>();
				for (String splitString : splitStrings) {
					
					int splitStringLength = splitString.length();
					String escapedCharacter = escapedCharacters.get(characterIndex);
					int startPosition = 0;
					int foundPosition = splitString.indexOf(escapedCharacter, startPosition);
					while (foundPosition >= 0) {
											
						// 次�?�開始�?置
						int nextStartPosition = foundPosition + escapedCharacter.length();
						
						// 置�?�対象文字�?��?�?対象文字を分割�?��?�リスト�?�追加�?�る
						if (foundPosition > startPosition) {
							splitStringBuffer.add(splitString.substring(startPosition, foundPosition));
						}
						splitStringBuffer.add(unescapedCharacters.get(characterIndex));
						
						// 開始�?置を更新�?��?�次を検索
						startPosition = nextStartPosition;
						if (startPosition < splitStringLength) {
							foundPosition = splitString.indexOf(escapedCharacter, startPosition);
						} else {
							foundPosition = -1;
						}
					}
					
					// 余�?��?�文字を格�?�?�る
					if (startPosition < splitStringLength) {
						splitStringBuffer.add(splitString.substring(startPosition));
					}
				}
				
				// 分割�?�れ�?�文字列を更新
				splitStrings = splitStringBuffer;
			}
			
			// 置�?��?�れ�?分割�?�れ�?��?�る文字列を統�?��?�る
			if (splitStrings.size() == 1) {
				value = splitStrings.get(0);
			} else {
				StringBuilder stringBuilder = new StringBuilder();
				for (String splitString : splitStrings) {
					stringBuilder.append(splitString);
				}
				value = stringBuilder.toString();
			}
		}
		
		return value;
	}
}
