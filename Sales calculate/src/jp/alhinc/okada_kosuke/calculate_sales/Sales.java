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

public class Sales {

	public static void main(String[] args) {

		String error="予期せぬエラーが発生しました";
		if(args.length !=1){
			System.out.println(error);
			return;
		}

		File branchfile = new File (args[0],  File.separator + "branch.lst" );
		File commodityfile = new File (args[0], File.separator + "commodity.lst" );
		File rcdfile = new File (args[0]);

		HashMap<String, String>branchNameMap = new HashMap<String,String>();
		HashMap<String,String>commoNameMap = new HashMap<String,String>();
		HashMap<String,Long> branchSaleMap = new HashMap<String,Long>();
		HashMap<String,Long> commoSaleMap = new HashMap<String,Long>();

		BufferedReader branchbuffer = null;
		BufferedReader commoditybuffer = null;





		if (!branchfile.exists() || !branchfile.canRead() ){
			System.out.println("支店定義ファイルが存在しません");
			return;
		}
		try{
			branchbuffer = new BufferedReader(new FileReader(branchfile));

			String s;

			while( (s = branchbuffer.readLine() ) != null){
				//System.out.println (s + "を読み込みました");
				String[] data = s.split(",");

				if(!data[0].matches("^[0-9]{3}$") || data.length !=2) {
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

		if (!commodityfile.exists() || !commodityfile.canRead() ){
			System.out.println("商品定義ファイルが存在しません");
			return;
		}
		try{
			commoditybuffer = new BufferedReader(new FileReader(commodityfile));

			String s;

			while((s=commoditybuffer.readLine())!=null){
				//System.out.println(s+"を読み込みました");
				String[] data = s.split(",");


				if(!data[0].matches("\\w{8}") || data.length !=2){
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
				commoNameMap.put(data[0], data[1]);
				commoSaleMap.put(data[0], 0L);
			}

		}catch(IOException e){
			System.out.println(error);
			return;
		}finally{
			if(commoditybuffer !=null){
				try{
					commoditybuffer.close();
				}catch(IOException e){
					System.out.println(error);
					return;
				}
			}
		}


		//String[] filelist = rcdfile.list();
		File[] filecheck = rcdfile.listFiles();
		ArrayList <Integer> rcdlist = new ArrayList <Integer>();
		ArrayList <String> rcdlist1 = new ArrayList <String>();
		for(int i=0;i <filecheck.length; i++){
			if(filecheck[i].getName().matches("^[0-9]{8}.rcd$") && filecheck[i].isFile() ){
				String[] rcddata = filecheck[i].getName().split( "\\.");
				rcdlist.add(Integer.valueOf(rcddata[0]) );
				rcdlist1.add(filecheck[i].getName());
			}
		}


		if(rcdlist.size() != rcdlist.get(rcdlist.size()-1) - rcdlist.get(0) + 1){
			System.out.println("売上ファイル名が連番になっていません");
			return;
		}

		BufferedReader br = null;
		BufferedWriter bw = null;
		BufferedWriter bw2 = null;

		//集計部門
		try{
			for(int i=0; i < rcdlist1.size(); i++){
				File file = new File (args[0], rcdlist1.get(i) );
				ArrayList <String> rcdRead = new ArrayList <String> ();
				br = new BufferedReader(new FileReader (file) );
				String s;
				while( (s=br.readLine() )!=null){
					rcdRead.add(s);
				}

				if (rcdRead.size() !=3){
					System.out.println(file.getName() + "のフォーマットが不正です");
					return;
				}

				if (!rcdRead.get(2).matches("^[0-9]*$")) {
					System.out.println(error);
					return;
				}
				//	System.out.println(rcdRead);

				if (! branchSaleMap.containsKey (rcdRead.get(0)) ){
					System.out.println(file.getName() + "の支店コードが不正です");
					return;
				}
				if (! commoSaleMap.containsKey(rcdRead.get(1)) ){
					System.out.println(file.getName() + "の商品コードが不正です");
					return;
				}

				branchSaleMap.put(rcdRead.get(0), Long.parseLong(rcdRead.get(2)) + branchSaleMap.get(rcdRead.get(0)) );
				commoSaleMap.put(rcdRead.get(1), Long.parseLong(rcdRead.get(2)) + commoSaleMap.get(rcdRead.get(1)) );

				int branchDigit = String.valueOf(branchSaleMap.get(rcdRead.get(0)) ).length();
				int commonDigit = String.valueOf(commoSaleMap.get(rcdRead.get(1)) ).length();
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
			List<Map.Entry <String,Long> > entries =
					new ArrayList <Map.Entry <String,Long> > (branchSaleMap.entrySet() );
			Collections.sort(entries, new Comparator<Map.Entry <String,Long> >() {
				public int compare(
						Entry <String,Long> entry1, Entry <String,Long> entry2) {
					return ((Long)entry2.getValue() ).compareTo((Long)entry1.getValue() );
				}
			});
			File file2 = new File (args[0],  File.separator + "branch.out");
			bw = new BufferedWriter(new FileWriter(file2) );
			for (Entry <String,Long> s : entries) {
				bw.write(s.getKey() + "," + branchNameMap.get(s.getKey() ) + "," + s.getValue() + System.getProperty("line.separator") );

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
			List <Map.Entry <String,Long> > entries2 =
					new ArrayList <Map.Entry <String,Long> > (commoSaleMap.entrySet() );
			Collections.sort(entries2, new Comparator<Map.Entry <String,Long> > () {
				public int compare(
						Entry <String,Long> entry1, Entry <String,Long> entry2) {
					return ((Long)entry2.getValue() ).compareTo((Long)entry1.getValue() );
				}
			});
			File file3 = new File (args[0], File.separator + "commodity.out");
			bw2 = new BufferedWriter ( new FileWriter(file3) );
			for (Entry <String,Long> s:entries2) {

				bw2.write(s.getKey() + "," + commoNameMap.get(s.getKey() ) + "," + s.getValue() + System.getProperty("line.separator") );
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