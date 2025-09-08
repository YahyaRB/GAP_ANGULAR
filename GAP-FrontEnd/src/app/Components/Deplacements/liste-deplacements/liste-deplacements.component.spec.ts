import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListeDeplacementsComponent } from './liste-deplacements.component';

describe('ListeDeplacementsComponent', () => {
  let component: ListeDeplacementsComponent;
  let fixture: ComponentFixture<ListeDeplacementsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ListeDeplacementsComponent]
    });
    fixture = TestBed.createComponent(ListeDeplacementsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
