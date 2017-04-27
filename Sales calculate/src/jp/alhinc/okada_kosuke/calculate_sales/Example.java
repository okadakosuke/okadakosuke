package jp.alhinc.okada_kosuke.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Example {
	public static void main(String[] args) {

		File branchfile = new File(args[0], "\\branch.lst");//支店コードファイル
		File commondityfile = new File(args[0],"\\commondity.lst");//商品コードファイル
		File rcdfile = new File(args[0]);//売上ファイル

		HashMap<String, String>branchNameMap = new HashMap<String,String>();//支店コードのsplit
		HashMap<String,String>commonNameMap = new HashMap<String,String>();//商品コードのsplit
		HashMap<String,Long> branchSaleMap = new HashMap<String,Long>();//支店コードの売上の箱
		HashMap<String,Long> commonSaleMap = new HashMap<String,Long>();//商品コードの売上の箱


		BufferedReader branchbuffer = null;//支店コードの読み取り
		BufferedReader commonditybuffer = null;//商品コードの読み取り


		String error="予期せぬエラーが発生しました";
		if(args.length !=1){
			System.out.println(error);
			return;
		}

		if (!branchfile.exists() || !branchfile.canRead()){
			System.out.println("支店定義ファイルが存在しません");
			return;
		}
		try{
			branchbuffer = new BufferedReader(new FileReader(branchfile));

			String s;

			while((s= branchbuffer.readLine())!=null){
				//System.out.println(s+"を読み込みました");
				String[] data = s.split(",");

				if(!data[0].matches("^[0-9]{3}$")){
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
				branchNameMap.put(data[0], data[1]);
				branchSaleMap.put(data[0], 0L);


			}
		}catch(IOException e){
			System.out.println(error);
			return;
		}finally{
			if(branchbuffer !=null){
				try{
					branchbuffer.close();
				}catch(IOException e){
					System.out.println(error);
					return;
				}
			}
		}





		if (!commondityfile.exists() || !commondityfile.canRead()){
			System.out.println("商品定義ファイルが存在しません");
			return;
		}
		try{
			commonditybuffer = new BufferedReader(new FileReader(commondityfile));


			String s;

			while((s=commonditybuffer.readLine())!=null){
				//System.out.println(s+"を読み込みました");
				String[] data = s.split(",");


				if(!data[0].matches("\\w{8}")){
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
				commonNameMap.put(data[0], data[1]);
				commonSaleMap.put(data[0], 0l);

			}

		}catch(IOException e){
			System.out.println(error);
			return;
		}finally{
			if(commonditybuffer !=null){
				try{
					commonditybuffer.close();
				}catch(IOException e){
					System.out.println(error);
					return;
				}
			}
		}

		String[] filelist = rcdfile.list();

		ArrayList<Integer> rcdlist = new ArrayList<Integer>();
		ArrayList<String> rcdlist1 = new ArrayList<String>();
		for(int i=0;i<filelist.length;i++){
			if(filelist[i].matches("^[0-9]{8}.rcd$")){
				String[] rcddata = filelist[i].split("\\.");
				rcdlist.add(Integer.valueOf(rcddata[0]));
				rcdlist1.add(filelist[i]);
			}
		}
		if(rcdlist.size() != rcdlist.get(rcdlist.size()-1) - rcdlist.get(0) + 1){
			System.out.println("売上ファイルが連番になっていません");
			return;
		}


		BufferedReader br = null;
		BufferedWriter bw = null;
		BufferedWriter bw2 = null;

		//集計部門
		try{
			for(int i=0; i < rcdlist1.size();i++){
				File file = new File (args[0], rcdlist1.get(i));//ファイル
				ArrayList<String> rcdRead = new ArrayList<String>();
				//		System.out.println(args[0] + "\\" + rcdlist1.get(i));
				br = new BufferedReader(new FileReader(file));//バッファードリーダー
				String s;
				while((s=br.readLine())!=null){//一行ずつ読み込む
					rcdRead.add(s);
				}
				//	System.out.println(rcdRead);
				if (rcdRead.size()>3){
					System.out.println(file.getName()+"のフォーマットが不正です");
					return;
				}

				if (! branchSaleMap.containsKey(rcdRead.get(0))){
					System.out.println(file.getName()+"の支店コードが不正です");
					return;
				}
				if (! commonSaleMap.containsKey(rcdRead.get(1))){
					System.out.println(file.getName()+"商品コードが不正です");
					return;
				}



				branchSaleMap.put(rcdRead.get(0), Long.parseLong(rcdRead.get(2)) + branchSaleMap.get(rcdRead.get(0)));
				//mapにrcdRead(arrayのやつ)の支店コードを入れる(rcdRead)に支店コードが対応、それに伴うrcdReadの売上+rcdReadの支店コードに対応する売上
				commonSaleMap.put(rcdRead.get(1), Long.parseLong(rcdRead.get(2)) + commonSaleMap.get(rcdRead.get(1)));


				int branchDigit = String.valueOf(branchSaleMap.get(rcdRead.get(0))).length();
				int commonDigit = String.valueOf(commonSaleMap.get(rcdRead.get(1))).length();
				if(branchDigit > 10 || commonDigit > 10){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

			}

		}catch(IOException e){
			System.out.println(error);
			return;
		}finally{
			if(br !=null){
				try{
					br.close();
				}catch(IOException e){
					System.out.println(error);
					return;
				}
			}
		}




		//System.out.println(branchSaleMap);

		//ソート部門
		try{
			List<Map.Entry<String,Long>> entries =
					new ArrayList<Map.Entry<String,Long>>(branchSaleMap.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {
				public int compare(
						Entry<String,Long> entry1, Entry<String,Long> entry2) {
					return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
			File file2 = new File (args[0], "\\branch.out");
			bw = new BufferedWriter(new FileWriter(file2));
			for (Entry<String,Long> s : entries) {
				bw.write(s.getKey() + "," + branchNameMap.get(s.getKey())+"," + s.getValue()+System.getProperty("line.separator"));
				//System.out.println("s.getKey() : " + s.getKey());
				//System.out.println("s.getValue() : " + s.getValue());
			}


		}catch(IOException e){
			System.out.println(error);
			return;
		}finally{
			if(bw !=null){
				try{bw.close();
				}catch(IOException e){
					System.out.println(error);
					return;
				}
			}
		}


		try{
			List<Map.Entry<String,Long>> entries2 =
					new ArrayList<Map.Entry<String,Long>>(commonSaleMap.entrySet());
			Collections.sort(entries2, new Comparator<Map.Entry<String,Long>>() {
				public int compare(
						Entry<String,Long> entry1, Entry<String,Long> entry2) {
					return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
			File file3 = new File (args[0],"\\commondity.out");
			bw2 = new BufferedWriter ( new FileWriter(file3));
			for (Entry<String,Long>s:entries2) {

				bw2.write(s.getKey() + "," + commonNameMap.get(s.getKey()) + "," + s.getValue() + System.getProperty("line.separator"));
				//System.out.println("s.getKey() : " + s.getKey());
				//System.out.println("s.getValue() : " + s.getValue());
			}

		}catch(IOException e){
			System.out.println(error);
			return;
		}finally{
			if(bw2 !=null){
				try{bw2.close();
				}catch(IOException e){
					System.out.println(error);
					return;
				}
			}
		}
	}
}