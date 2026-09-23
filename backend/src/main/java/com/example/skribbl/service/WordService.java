package com.example.skribbl.service;
import com.example.skribbl.entity.Word;
import com.example.skribbl.repository.WordRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.util.*;
@Service
public class WordService {
    private final WordRepository repo;
    private final List<String> seed=List.of(
      "elephant","tiger","house","pizza","football","computer","rocket","guitar","banana",
      "mountain","doctor","airplane","rainbow","camera","dragon","hamburger","bicycle",
      "robot","castle","penguin","keyboard","volcano","sandwich","basketball","lion",
      "tree","school","phone","sunflower","beach");
    public WordService(WordRepository repo){this.repo=repo;}
    @PostConstruct public void seed(){if(repo.count()==0)for(String s:seed){Word w=new Word();w.setText(s);w.setCategory("GENERAL");repo.save(w);}}
    public List<String> choose(int count){List<String> a=new ArrayList<>(repo.findAll().stream().map(Word::getText).toList());Collections.shuffle(a);return a.subList(0,Math.min(count,a.size()));}
    public String normalize(String s){return s==null?"":s.trim().replaceAll("\\s+"," ").toLowerCase(Locale.ROOT);}
    public boolean matches(String guess,String actual){return normalize(guess).equals(normalize(actual));}
    public String hint(String word,int reveal){char[] x=new char[word.length()];Arrays.fill(x,'_');for(int i=0;i<Math.min(reveal,word.length());i++)x[i]=word.charAt(i);return new String(x);}
}
