package com.example.demo;

        import com.example.demo.model.Item;

        import com.example.demo.repository.ItemRepository;

        import org.springframework.beans.factory.annotation.Autowired;

        import org.springframework.boot.CommandLineRunner;

        import org.springframework.stereotype.Component;

@Component

public class DataLoader implements CommandLineRunner {

    @Autowired

    private ItemRepository itemRepository;

    @Override

    public void run(String... args) throws Exception {

        // Controlla se l'item "nessun oggetto" esiste già

        if (itemRepository.findByName("nessun oggetto").isEmpty()) {

            // Crea e salva l'item "nessun oggetto"

            Item item = new Item("nessun oggetto");

            itemRepository.save(item);

        }

    }

}

