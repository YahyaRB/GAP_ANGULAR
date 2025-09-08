import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListeChauffeursComponent } from './liste-chauffeurs.component';

describe('ListeChauffeursComponent', () => {
  let component: ListeChauffeursComponent;
  let fixture: ComponentFixture<ListeChauffeursComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ListeChauffeursComponent]
    });
    fixture = TestBed.createComponent(ListeChauffeursComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
