package com.example.hotelbooking.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.hotelbooking.data.repository.WishlistRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WishlistViewModel extends ViewModel {
    private final WishlistRepository wishlistRepository = new WishlistRepository();

    private final MutableLiveData<List<Map<String, Object>>> _wishlistItems = new MutableLiveData<>();
    public LiveData<List<Map<String, Object>>> wishlistItems = _wishlistItems;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public void fetchWishlist(String userId) {
        wishlistRepository.getWishlist(userId)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Map<String, Object> data = doc.getData();
                            if (data != null) {
                                data.put("hotelId", doc.getId());
                                list.add(data);
                            }
                        }
                    }
                    _wishlistItems.setValue(list);
                })
                .addOnFailureListener(e -> _error.setValue(e.getMessage()));
    }
}