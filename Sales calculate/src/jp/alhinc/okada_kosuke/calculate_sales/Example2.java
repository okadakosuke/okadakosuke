package jp.alhinc.okada_kosuke.calculate_sales;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Example2 {
	public static void main(String[] args) {
		try{
			File file = new File(args[0],"\\commondity.lst");


			HashMap<String,String>map= new HashMap<String,String>();


			FileReader fw = new FileReader(file);
			BufferedReader br = new BufferedReader(fw);


			String s;

			while((s= br.readLine())!=null){
				System.out.println(s+"を読み込みました");
				String[] data = s.split(",");
				map.put(data[0], data[1]);


				if (!file.exists() || !file.canRead()){
					System.out.println("商品定義ファイルが存在しません");
					return;

				}

				String st = "date[0]";
				String str = "date[1]";

				if(!st.matches("~[0-9][3]$")){
					System.out.println("フォーマットが不正です");



				}
			}
			br.close();
		}catch(IOException e){
			System.out.println("商品低費");

		}
	}

}
