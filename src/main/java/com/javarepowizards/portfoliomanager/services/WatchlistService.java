package com.javarepowizards.portfoliomanager.services;

import com.javarepowizards.portfoliomanager.dao.IWatchlistDAO;
import com.javarepowizards.portfoliomanager.domain.stock.StockRepository;
import com.javarepowizards.portfoliomanager.models.Watchlist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WatchlistService {
    private final Watchlist watchlist;

    @Autowired
    public WatchlistService(StockRepository repo, IWatchlistDAO watchlistDAO) {
        this.watchlist = new Watchlist(repo, watchlistDAO, getCurrentUserId());
    }

    public Watchlist getWatchlist() {
        return watchlist;
    }

    private int getCurrentUserId() {
        return 1;
    }
}
